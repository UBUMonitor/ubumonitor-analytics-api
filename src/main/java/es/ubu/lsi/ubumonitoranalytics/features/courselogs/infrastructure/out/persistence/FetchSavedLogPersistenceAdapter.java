package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.FetchLogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.*;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;

@Component
@RequiredArgsConstructor
public class FetchSavedLogPersistenceAdapter implements FetchLogPersistencePort {

    private final Jooq jooq;

    // =================================================================================
    // API PRINCIPAL
    // =================================================================================

    @Override
    public FetchCourseLogsResult getLogs(CourseLogsInfoRequest request) {
        CourseLogsInfoRequestPagination pagination = request.getPagination();
        List<Condition> conditions = buildConditions(request);
        List<FetchLogLine> logs = fetchLogLines(request, conditions, pagination);

        return buildResult(request, logs, conditions, pagination);
    }

    // =================================================================================
    // EJECUCIÓN DE QUERIES Y CONSTRUCCIÓN DE RESULTADOS
    // =================================================================================

    private @NonNull List<FetchLogLine> fetchLogLines(
        CourseLogsInfoRequest request,
        List<Condition> conditions,
        CourseLogsInfoRequestPagination pagination
    ) {
        List<LogViewColumn> columns = resolveColumns(request);
        Set<LogViewColumn> requiredColumns = determineRequiredColumns(columns, request.getSort());
        List<SelectFieldOrAsterisk> fields = mapToSelectFields(columns);

        return buildDynamicQuery(fields, requiredColumns)
            .where(conditions)
            .orderBy(buildSortFields(request.getSort()))
            .offset(pagination.getPage() * pagination.getSize())
            .limit(pagination.getSize())
            .fetchInto(FetchLogLine.class);
    }

    private FetchCourseLogsResult buildResult(
        CourseLogsInfoRequest request,
        List<FetchLogLine> logs,
        List<Condition> conditions,
        CourseLogsInfoRequestPagination pagination
    ) {
        FetchCourseLogsResult.FetchCourseLogsResultBuilder builder = FetchCourseLogsResult.builder()
            .logs(logs)
            .page(pagination.getPage())
            .size(logs.size())
            .empty(logs.isEmpty());

        if (Boolean.TRUE.equals(request.getIncludeTotal())) {
            int total = fetchTotal(conditions);
            int totalPages = (int) Math.ceil((double) total / pagination.getSize());

            builder.totalElements(total)
                .totalPages(totalPages)
                .first(pagination.getPage() == 0)
                .last(pagination.getPage() >= Math.max(0, totalPages - 1));
        }

        return builder.build();
    }

    private int fetchTotal(List<Condition> conditions) {
        Integer totalRecord = jooq.dsl()
            .selectCount()
            .from(LOGS)
            .where(conditions)
            .fetchOneInto(Integer.class);

        return totalRecord != null ? totalRecord : 0;
    }

    // =================================================================================
    // CONSTRUCCIÓN DINÁMICA DE JOOQ (SELECT & JOINS)
    // =================================================================================

    private SelectJoinStep<Record> buildDynamicQuery(
        List<SelectFieldOrAsterisk> fields,
        Set<LogViewColumn> requiredColumns
    ) {
        SelectJoinStep<Record> query = jooq.dsl().select(fields).from(LOGS);

        if (requiredColumns.contains(LogViewColumn.USER_FULL_NAME)) {
            query = query.leftJoin(USERS).on(LOGS.USER_ID.eq(USERS.ID));
        }
        if (requiredColumns.contains(LogViewColumn.MODULE_NAME)) {
            query = query.leftJoin(MODULES).on(LOGS.MODULE_ID.eq(MODULES.ID));
        }
        if (requiredColumns.contains(LogViewColumn.COMPONENT_NAME)) {
            query = query.join(LOGS_COMPONENTS).on(LOGS.COMPONENT_ID.eq(LOGS_COMPONENTS.ID));
        }
        if (requiredColumns.contains(LogViewColumn.EVENT_NAME)) {
            query = query.join(LOGS_EVENTS).on(LOGS.EVENT_ID.eq(LOGS_EVENTS.ID));
        }
        if (requiredColumns.contains(LogViewColumn.ORIGIN_NAME)) {
            query = query.join(LOGS_ORIGINS).on(LOGS.ORIGIN_ID.eq(LOGS_ORIGINS.ID));
        }

        return query;
    }

    private List<Condition> buildConditions(CourseLogsInfoRequest request) {
        List<Condition> conditions = new ArrayList<>();
        CourseLogsInfoRequestTimeRange timeRange = request.getTimeRange();
        CourseLogsInfoRequestFilters filters = request.getFilters();

        // 1. Filtros base y de tiempo
        conditions.add(LOGS.COURSE_ID.eq(request.getCourseId()));

        if (timeRange != null) {
            if (timeRange.getFrom() != null) {
                conditions.add(LOGS.TIMESTAMP.greaterOrEqual(timeRange.getFrom()));
            }
            if (timeRange.getTo() != null) {
                conditions.add(LOGS.TIMESTAMP.lessOrEqual(timeRange.getTo()));
            }
        }

        // 2. Filtros dinámicos
        if (filters != null) {
            addInFilter(conditions, LOGS.USER_ID, filters.getUserIds());
            addInFilter(conditions, LOGS.MODULE_ID, filters.getModuleIds());
            addInFilter(conditions, LOGS.ORIGIN_ID, filters.getOriginIds());
            addInFilter(conditions, LOGS.COMPONENT_ID, filters.getComponentIds());
            addInFilter(conditions, LOGS.EVENT_ID, filters.getEventIds());
            addInFilter(conditions, LOGS.IP_ADDRESS, filters.getIpAddresses());
        }

        return conditions;
    }

    private <T> void addInFilter(List<Condition> conditions, Field<T> field, Collection<T> values) {
        if (values == null) return;

        if (values.isEmpty()) {
            conditions.add(DSL.falseCondition());
        } else {
            conditions.add(field.in(values));
        }
    }

    private List<SortField<?>> buildSortFields(List<CourseLogsInfoRequestSortItem> sortList) {
        if (sortList == null || sortList.isEmpty()) {
            return List.of(LOGS.TIMESTAMP.asc());
        }

        return sortList.stream()
            .map(sort -> {
                Field<?> field = fieldFor(sort.getField());
                if (field == null) return null;

                return sort.getDirection() == CourseLogsInfoRequestSortItem.Direction.DESC
                    ? field.desc()
                    : field.asc();
            })
            .filter(Objects::nonNull)
            .toList();
    }

    // =================================================================================
    // MAPPERS Y HELPERS DE COLUMNAS
    // =================================================================================

    private List<LogViewColumn> resolveColumns(CourseLogsInfoRequest request) {
        List<LogViewColumn> fields = request.getFields();
        return (fields == null || fields.isEmpty())
            ? List.of(LogViewColumn.values())
            : fields;
    }

    private Set<LogViewColumn> determineRequiredColumns(
        List<LogViewColumn> columns,
        List<CourseLogsInfoRequestSortItem> sort
    ) {
        Set<LogViewColumn> required = new HashSet<>(columns);
        if (sort != null) {
            sort.forEach(s -> required.add(s.getField()));
        }
        return required;
    }

    private List<SelectFieldOrAsterisk> mapToSelectFields(List<LogViewColumn> columns) {
        return columns.stream()
            .map(this::fieldFor)
            .filter(Objects::nonNull)
            .map(SelectFieldOrAsterisk.class::cast)
            .toList();
    }

    private Field<?> fieldFor(LogViewColumn column) {
        return switch (column) {
            case ID -> LOGS.ID;
            case TIMESTAMP -> LOGS.TIMESTAMP;
            case COURSE_ID -> LOGS.COURSE_ID;
            case USER_ID -> LOGS.USER_ID;
            case MODULE_ID -> LOGS.MODULE_ID;
            case COMPONENT_ID -> LOGS.COMPONENT_ID;
            case EVENT_ID -> LOGS.EVENT_ID;
            case ORIGIN_ID -> LOGS.ORIGIN_ID;
            case IP_ADDRESS -> LOGS.IP_ADDRESS;
            case USER_FULL_NAME -> USERS.FULL_NAME.as("USER_FULL_NAME");
            case MODULE_NAME -> MODULES.NAME.as("MODULE_NAME");
            case COMPONENT_NAME -> LOGS_COMPONENTS.NAME.as("COMPONENT_NAME");
            case EVENT_NAME -> LOGS_EVENTS.NAME.as("EVENT_NAME");
            case ORIGIN_NAME -> LOGS_ORIGINS.NAME.as("ORIGIN_NAME");
        };
    }
}
