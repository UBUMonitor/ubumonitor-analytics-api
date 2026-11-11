package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sections.SECTIONS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.FetchLogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list.*;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.Record;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Queries tenant persistence for filtered course logs with optional pagination. */
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
    Condition conditions = buildConditions(request);
    List<FetchLogLine> logs = fetchLogLines(request, conditions, pagination);

    return buildResult(request, logs, conditions, pagination);
  }

  // =================================================================================
  // Query execution and result construction.
  // =================================================================================

  private @NonNull List<FetchLogLine> fetchLogLines(
      CourseLogsInfoRequest request,
      Condition conditions,
      CourseLogsInfoRequestPagination pagination) {
    List<LogViewColumn> columns = resolveColumns(request);
    Set<LogViewColumn> requiredColumns = determineRequiredColumns(columns, request.getSort());
    List<SelectFieldOrAsterisk> fields = mapToSelectFields(columns);

    var query =
        buildDynamicQuery(fields, requiredColumns)
            .where(conditions)
            .orderBy(buildSortFields(request.getSort()));

    if (pagination == null) {
      return query.fetchInto(FetchLogLine.class);
    }

    return query
        .offset(pagination.getPage() * pagination.getSize())
        .limit(pagination.getSize())
        .fetchInto(FetchLogLine.class);
  }

  private FetchCourseLogsResult buildResult(
      CourseLogsInfoRequest request,
      List<FetchLogLine> logs,
      Condition conditions,
      CourseLogsInfoRequestPagination pagination) {
    FetchCourseLogsResult.FetchCourseLogsResultBuilder builder =
        FetchCourseLogsResult.builder()
            .logs(logs)
            .page(pagination == null ? 0 : pagination.getPage())
            .size(logs.size())
            .empty(logs.isEmpty());

    if (Boolean.TRUE.equals(request.getIncludeTotal())) {
      int total = fetchTotal(conditions);
      int totalPages =
          pagination == null
              ? (total == 0 ? 0 : 1)
              : (int) Math.ceil((double) total / pagination.getSize());

      builder
          .totalElements(total)
          .totalPages(totalPages)
          .first(pagination == null || pagination.getPage() == 0)
          .last(pagination == null || pagination.getPage() >= Math.max(0, totalPages - 1));
    }

    return builder.build();
  }

  private int fetchTotal(Condition conditions) {
    Integer totalRecord =
        jooq.dsl().selectCount().from(LOGS).where(conditions).fetchOneInto(Integer.class);

    return totalRecord != null ? totalRecord : 0;
  }

  // =================================================================================
  // Dynamic jOOQ query construction (SELECT and JOIN clauses).
  // =================================================================================

  private SelectJoinStep<Record> buildDynamicQuery(
      List<SelectFieldOrAsterisk> fields, Set<LogViewColumn> requiredColumns) {
    SelectJoinStep<Record> query = jooq.dsl().select(fields).from(LOGS);

    // MODULES is required for MODULE_NAME and for resolving section fields,
    // because sections are associated through modules.
    boolean needsModules =
        requiredColumns.contains(LogViewColumn.MODULE_NAME)
            || requiredColumns.contains(LogViewColumn.SECTION_ID)
            || requiredColumns.contains(LogViewColumn.SECTION_NAME);

    if (needsModules) {
      query = query.leftJoin(MODULES).on(LOGS.MODULE_ID.eq(MODULES.ID));
    }
    if (requiredColumns.contains(LogViewColumn.USER_FULL_NAME)) {
      query = query.leftJoin(USERS).on(LOGS.USER_ID.eq(USERS.ID));
    }
    if (requiredColumns.contains(LogViewColumn.COMPONENT_NAME)) {
      query = query.join(LOGS_COMPONENTS).on(LOGS.COMPONENT_ID.eq(LOGS_COMPONENTS.ID));
    }
    if (requiredColumns.contains(LogViewColumn.EVENT_NAME)) {
      query = query.join(LOGS_EVENTS).on(LOGS.EVENT_ID.eq(LOGS_EVENTS.ID));
    }
    if (requiredColumns.contains(LogViewColumn.ORIGIN_NAME)
        || requiredColumns.contains(LogViewColumn.ORIGIN_CODE)) {
      query = query.join(LOGS_ORIGINS).on(LOGS.ORIGIN_ID.eq(LOGS_ORIGINS.ID));
    }
    if (requiredColumns.contains(LogViewColumn.SECTION_NAME)) {
      query = query.leftJoin(SECTIONS).on(MODULES.SECTION_ID.eq(SECTIONS.ID));
    }

    return query;
  }

  private Condition buildConditions(CourseLogsInfoRequest request) {
    CourseLogsInfoRequestTimeRange timeRange = request.getTimeRange();
    CourseLogsInfoRequestFilters filters = request.getFilters();

    Condition condition = LOGS.COURSE_ID.eq(request.getCourseId());

    if (timeRange != null) {
      condition = condition.and(LogConditions.timeRange(timeRange.getFrom(), timeRange.getTo()));
    }

    if (filters != null) {
      condition =
          condition
              .and(
                  LogConditions.filters(
                      filters.getUserIds(),
                      filters.getModuleIds(),
                      filters.getComponentIds(),
                      filters.getEventIds(),
                      filters.getOriginIds(),
                      filters.getIpAddresses(),
                      filters.getSectionIds()))
              .and(CompositeLogFilterConditions.build(filters.getCompositeFilters()));
    }

    return condition;
  }

  private List<SortField<?>> buildSortFields(List<CourseLogsInfoRequestSortItem> sortList) {
    if (sortList == null || sortList.isEmpty()) {
      return List.of(LOGS.LOGGED_AT.asc());
    }

    List<SortField<?>> sortFields = new ArrayList<>();
    for (CourseLogsInfoRequestSortItem sort : sortList) {
      Field<?> field = fieldFor(sort.getField());
      if (field != null) {
        sortFields.add(
            sort.getDirection() == CourseLogsInfoRequestSortItem.Direction.DESC
                ? field.desc()
                : field.asc());
      }
    }
    return sortFields;
  }

  // =================================================================================
  // MAPPERS Y HELPERS DE COLUMNAS
  // =================================================================================

  private List<LogViewColumn> resolveColumns(CourseLogsInfoRequest request) {
    List<LogViewColumn> fields = request.getFields();
    return (fields == null || fields.isEmpty()) ? List.of(LogViewColumn.values()) : fields;
  }

  private Set<LogViewColumn> determineRequiredColumns(
      List<LogViewColumn> columns, List<CourseLogsInfoRequestSortItem> sort) {
    Set<LogViewColumn> required = new HashSet<>(columns);
    if (sort != null) {
      for (CourseLogsInfoRequestSortItem sortItem : sort) {
        required.add(sortItem.getField());
      }
    }
    return required;
  }

  private List<SelectFieldOrAsterisk> mapToSelectFields(List<LogViewColumn> columns) {
    List<SelectFieldOrAsterisk> fields = new ArrayList<>();
    for (LogViewColumn column : columns) {
      Field<?> field = fieldFor(column);
      if (field != null) {
        fields.add(field);
      }
    }
    return fields;
  }

  private Field<?> fieldFor(LogViewColumn column) {
    return switch (column) {
      case ID -> LOGS.ID;
      case TIMESTAMP -> LOGS.LOGGED_AT.as("TIMESTAMP");
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
      case ORIGIN_CODE -> LOGS_ORIGINS.CODE.as("ORIGIN_CODE");
      case SECTION_ID -> MODULES.SECTION_ID.as("SECTION_ID");
      case SECTION_NAME -> SECTIONS.NAME.as("SECTION_NAME");
    };
  }
}
