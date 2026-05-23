package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;


import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ColumnsSort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.Direction;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.GetCourseLogsCommand;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.FetchLogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.LogViewColumn;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsView.LOGS_VIEW;

@Component
@RequiredArgsConstructor
public class FetchSavedLogsLogPersistenceAdapter implements FetchLogPersistencePort {

    private final Jooq jooq;

    @Override
    public FetchCourseLogsResult getLogs(GetCourseLogsCommand cmd) {
        Integer page = cmd.getPageableRequest().getPage();
        Integer limit = cmd.getPageableRequest().getSize();

        List<Condition> conditions = getConditions(cmd);

        List<FetchLogLine> logs = getFetchLogLines(cmd, conditions);
        Integer total = null;
        Integer totalPages = null;
        Boolean first = null;
        Boolean last = null;

        if (Boolean.TRUE.equals(cmd.getIncludeTotal())) {
            total = getTotal(conditions);
            totalPages = (int) Math.ceil((double) total / limit);
            first = page == 0;
            last = page >= totalPages - 1;
        }

        return FetchCourseLogsResult.builder()
            .logs(logs)
            .page(page)
            .size(logs.size())
            .totalElements(total)
            .totalPages(totalPages)
            .first(first)
            .last(last)
            .empty(logs.isEmpty())
            .build();
    }

    private int getTotal(List<Condition> conditions) {

        Integer totalRecord = jooq.dsl()
            .selectCount()
            .from(LOGS_VIEW)
            .where(conditions)
            .fetchOneInto(Integer.class);

        return (totalRecord != null) ? totalRecord : 0;
    }

    private @NonNull List<FetchLogLine> getFetchLogLines(GetCourseLogsCommand cmd, List<Condition> conditions) {


        Integer page = cmd.getPageableRequest().getPage();
        Integer limit = cmd.getPageableRequest().getSize();

        List<? extends Field<?>> fields = getSelectedFields(cmd.getColumns());
        List<SortField<?>> sortFields = getSortFields(cmd.getPageableRequest().getSort());


        return jooq.dsl()
            .select(fields)
            .from(LOGS_VIEW)
            .where(conditions)
            .orderBy(sortFields)
            .offset(page * limit)
            .limit(limit)
            .fetchInto(FetchLogLine.class);
    }

    private List<? extends Field<?>> getSelectedFields(List<LogViewColumn> columns) {

        Field<?>[] tableFields = LOGS_VIEW.fields();

        if (columns == null || columns.isEmpty()) {
            return Arrays.asList(tableFields);
        }

        Map<String, Field<?>> fieldMap = Arrays.stream(tableFields)
            .collect(Collectors.toMap(Field::getName, f -> f));

        return columns.stream()
            .map(c -> fieldMap.get(c.name()))
            .filter(Objects::nonNull)
            .toList();
    }

    private List<Condition> getConditions(GetCourseLogsCommand cmd) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(LOGS_VIEW.COURSE_ID.eq(cmd.getCourseId()));
        if (cmd.getStartDateTime() != null) {
            conditions.add(LOGS_VIEW.TIMESTAMP.greaterOrEqual(cmd.getStartDateTime()));
        }

        if (cmd.getEndDateTime() != null) {
            conditions.add(LOGS_VIEW.TIMESTAMP.lessOrEqual(cmd.getEndDateTime()));
        }

        addFilter(conditions, LOGS_VIEW.USER_ID, cmd.getUsersIds());
        addFilter(conditions, LOGS_VIEW.MODULE_ID, cmd.getModulesIds());

        addFilter(conditions, LOGS_VIEW.ORIGIN_ID, cmd.getOriginsIds());
        addFilter(conditions, LOGS_VIEW.COMPONENT_ID, cmd.getComponentsIds());
        addFilter(conditions, LOGS_VIEW.EVENT_ID, cmd.getEventsIds());
        addFilter(conditions, LOGS_VIEW.IP_ADDRESS, cmd.getIpAddresses());
        return conditions;

    }

    private <T> void addFilter(List<Condition> conditions,
                               Field<T> field,
                               Collection<T> values) {
        if (values == null) {
            return;
        }

        if (values.isEmpty()) {
            conditions.add(DSL.falseCondition());
            return;
        }

        conditions.add(field.in(values));
    }

    private List<SortField<?>> getSortFields(List<ColumnsSort> sortList) {

        if (sortList == null || sortList.isEmpty()) {
            return List.of(LOGS_VIEW.TIMESTAMP.asc());
        }

        Field<?>[] tableFields = LOGS_VIEW.fields();

        Map<String, Field<?>> fieldMap = Arrays.stream(tableFields)
            .collect(Collectors.toMap(Field::getName, f -> f));

        return sortList.stream()
            .map(sort -> {
                Field<?> field = fieldMap.get(sort.getColumn().name());

                if (field == null) {
                    return null;
                }

                return sort.getDirection() == Direction.DESC
                    ? field.desc()
                    : field.asc();
            })
            .filter(Objects::nonNull)
            .toList();
    }

}
