package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sections.SECTIONS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogsStatisticsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.*;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/** Calculates requested course log aggregates with jOOQ. */
@Component
@RequiredArgsConstructor
public class LogsStatisticsAdapter implements LogsStatisticsPort {

  public static final String TIME_BUCKET = "timeBucket";
  public static final String VALUE = "VALUE";
  private final Jooq jooq;

  @Override
  public CourseLogsStatisticsResult getCourseLogsStatistics(CourseLogsStatisticsRequest request) {

    List<Condition> conditions = buildConditions(request);

    Set<Field<?>> innerGroupBy = resolveInnerGroupByFields(request);

    boolean needsSectionInner = needsSection(request);

    List<SelectField<?>> innerSelect = new ArrayList<>(innerGroupBy);
    innerSelect.add(DSL.count().as(VALUE));

    SelectJoinStep<Record> innerFrom = jooq.dsl().select(innerSelect).from(LOGS);

    // =========================
    // INNER JOIN PHASE
    // (needed when a column used in GROUP BY / SELECT of the
    // inner aggregation lives in a table other than LOGS, e.g.
    // MODULES.SECTION_ID)
    // =========================
    if (needsSectionInner) {
      innerFrom = innerFrom.leftJoin(MODULES).on(LOGS.MODULE_ID.eq(MODULES.ID));
    }

    Table<Record> aggregatedLogs =
        innerFrom.where(conditions).groupBy(innerGroupBy).asTable("agg_logs");

    List<SelectField<?>> outerSelect = resolveOuterSelectFields(request, aggregatedLogs);

    // =========================
    // OUTER JOIN PHASE
    // =========================
    SelectJoinStep<Record> query = jooq.dsl().select(outerSelect).from(aggregatedLogs);

    query = addOptimizedJoins(query, request.getFields(), aggregatedLogs);

    // =========================
    // ORDER BY GROUP BY FIELDS
    // =========================
    Select<?> finalQuery = applyOrderBy(query, request, aggregatedLogs);

    List<StatisticRow> rows = finalQuery.fetchInto(StatisticRow.class);

    return CourseLogsStatisticsResult.builder().rows(rows).build();
  }

  private Select<?> applyOrderBy(
      SelectJoinStep<Record> query,
      CourseLogsStatisticsRequest request,
      Table<Record> aggregatedLogs) {

    if (request.getSortBy() == null || request.getSortBy().isEmpty()) {
      return query;
    }

    List<OrderField<?>> orderFields = new ArrayList<>();

    for (SortBy sort : request.getSortBy()) {
      orderFields.add(resolveOrderField(sort, aggregatedLogs));
    }

    return query.orderBy(orderFields);
  }

  // =========================================================
  // ORDER BY = GROUP BY FIELDS
  // =========================================================
  private OrderField<?> resolveOrderField(SortBy sort, Table<Record> aggregatedLogs) {

    Field<?> field =
        switch (sort.getField()) {
          case VALUE -> aggregatedLogs.field(VALUE, Integer.class);

          case USER_ID -> aggregatedLogs.field(LOGS.USER_ID);

          case MODULE_ID -> aggregatedLogs.field(LOGS.MODULE_ID);

          case TIME_BUCKET -> aggregatedLogs.field(TIME_BUCKET);

          case TIME -> aggregatedLogs.field(TIME_BUCKET); // Logical alias.
        };

    return sort.getDirection() == SortBy.SortDirection.ASC ? field.asc() : field.desc();
  }

  // =========================================================
  // JOINS
  // =========================================================
  private SelectJoinStep<Record> addOptimizedJoins(
      SelectJoinStep<Record> query,
      List<CourseLogsSelectColumn> selection,
      Table<Record> aggregatedLogs) {

    if (selection == null) return query;

    if (selection.contains(CourseLogsSelectColumn.USER_FULL_NAME)) {
      query = query.leftJoin(USERS).on(USERS.ID.eq(aggregatedLogs.field(LOGS.USER_ID)));
    }

    if (selection.contains(CourseLogsSelectColumn.MODULE_NAME)) {
      query = query.leftJoin(MODULES).on(MODULES.ID.eq(aggregatedLogs.field(LOGS.MODULE_ID)));
    }

    if (selection.contains(CourseLogsSelectColumn.COMPONENT_NAME)) {
      query =
          query
              .leftJoin(LOGS_COMPONENTS)
              .on(LOGS_COMPONENTS.ID.eq(aggregatedLogs.field(LOGS.COMPONENT_ID)));
    }

    if (selection.contains(CourseLogsSelectColumn.EVENT_NAME)) {
      query =
          query.leftJoin(LOGS_EVENTS).on(LOGS_EVENTS.ID.eq(aggregatedLogs.field(LOGS.EVENT_ID)));
    }

    if (selection.contains(CourseLogsSelectColumn.ORIGIN_NAME)) {
      query =
          query.leftJoin(LOGS_ORIGINS).on(LOGS_ORIGINS.ID.eq(aggregatedLogs.field(LOGS.ORIGIN_ID)));
    }

    // SECTION_ID is already projected by the inner aggregation
    // (see needsSection()/innerFrom join to MODULES), so here we
    // only need to join SECTIONS to resolve the human-readable name.
    if (selection.contains(CourseLogsSelectColumn.SECTION_NAME)) {
      query = query.leftJoin(SECTIONS).on(SECTIONS.ID.eq(aggregatedLogs.field(MODULES.SECTION_ID)));
    }

    return query;
  }

  // =========================================================
  // GROUP BY
  // =========================================================
  private Set<Field<?>> resolveInnerGroupByFields(CourseLogsStatisticsRequest request) {

    Set<Field<?>> innerGroupBy = new LinkedHashSet<>();

    if (request.getGroupBy() != null) {
      for (CourseLogsGroupBy g : request.getGroupBy()) {
        innerGroupBy.add(impliedIdFieldFor(g.name()));
      }
    }

    if (request.getFields() != null) {
      for (CourseLogsSelectColumn s : request.getFields()) {
        innerGroupBy.add(impliedIdFieldFor(s.name()));
      }
    }

    if (request.getInterval() != null) {
      innerGroupBy.add(resolveIntervalField(request.getInterval()));
    }

    return innerGroupBy;
  }

  // =========================================================
  // Does this request need MODULES joined inside the inner
  // aggregation (because it groups/selects by SECTION)?
  // =========================================================
  private boolean needsSection(CourseLogsStatisticsRequest request) {

    boolean inGroupBy =
        request.getGroupBy() != null
            && request.getGroupBy().stream().anyMatch(g -> g.name().contains("SECTION"));

    boolean inFields =
        request.getFields() != null
            && request.getFields().stream().anyMatch(f -> f.name().contains("SECTION"));

    return inGroupBy || inFields;
  }

  // =========================================================
  // OUTER SELECT
  // =========================================================
  private List<SelectField<?>> resolveOuterSelectFields(
      CourseLogsStatisticsRequest request, Table<Record> aggregatedLogs) {

    Set<SelectField<?>> fields = new LinkedHashSet<>();

    if (request.getInterval() != null) {
      fields.add(aggregatedLogs.field(TIME_BUCKET, String.class));
    }

    if (request.getGroupBy() != null) {
      for (CourseLogsGroupBy g : request.getGroupBy()) {
        fields.add(outerSelectFieldForGroup(g, aggregatedLogs));
      }
    }

    if (request.getFields() != null) {
      for (CourseLogsSelectColumn col : request.getFields()) {
        fields.add(outerSelectFieldFor(col, aggregatedLogs));
      }
    }

    fields.add(aggregatedLogs.field(VALUE, Integer.class));

    return new ArrayList<>(fields);
  }

  // =========================================================
  // TIME BUCKET
  // =========================================================
  private Field<?> resolveIntervalField(TimeInterval interval) {

    if (interval == TimeInterval.DAY_OF_WEEK) {
      return dayOfWeekField(LOGS.LOGGED_AT);
    }

    if (interval == TimeInterval.WEEKLY) {
      return weekBucketField(LOGS.LOGGED_AT);
    }

    String sqlitePattern =
        switch (interval) {
          case HOURLY -> "%Y-%m-%dT%H:00:00";
          case DAILY -> "%Y-%m-%d";
          case MONTHLY -> "%Y-%m";
          case WEEKLY, DAY_OF_WEEK -> throw new IllegalStateException("Handled above");
        };
    return DSL.field(
            "strftime({0}, {1}, 'unixepoch')",
            String.class, DSL.inline(sqlitePattern), LOGS.LOGGED_AT)
        .as(TIME_BUCKET);
  }

  public static Field<Integer> dayOfWeekField(Field<?> timestamp) {
    return DSL.field(
            "(CAST(strftime('%w', {0}, 'unixepoch') AS INTEGER) + 6) % 7 + 1",
            Integer.class, timestamp)
        .as(TIME_BUCKET);
  }

  public static Field<String> weekBucketField(Field<?> timestamp) {
    return DSL.field("strftime('%G-W%V', {0}, 'unixepoch')", String.class, timestamp)
        .as(TIME_BUCKET);
  }

  // =========================================================
  // FIELD MAPPING
  // =========================================================
  private Field<?> impliedIdFieldFor(String columnOrGroup) {
    if (columnOrGroup.contains("USER")) return LOGS.USER_ID;
    if (columnOrGroup.contains("MODULE")) return LOGS.MODULE_ID;
    if (columnOrGroup.contains("COMPONENT")) return LOGS.COMPONENT_ID;
    if (columnOrGroup.contains("EVENT")) return LOGS.EVENT_ID;
    if (columnOrGroup.contains("ORIGIN")) return LOGS.ORIGIN_ID;
    if (columnOrGroup.contains("IP_ADDRESS")) return LOGS.IP_ADDRESS;
    if (columnOrGroup.contains("COURSE_ID")) return LOGS.COURSE_ID;
    if (columnOrGroup.contains("SECTION")) return MODULES.SECTION_ID;

    throw new IllegalArgumentException("Unknown field: " + columnOrGroup);
  }

  private Field<?> outerSelectFieldForGroup(
      CourseLogsGroupBy groupBy, Table<Record> aggregatedLogs) {
    return switch (groupBy) {
      case USER_ID -> aggregatedLogs.field(LOGS.USER_ID);
      case MODULE_ID -> aggregatedLogs.field(LOGS.MODULE_ID);
      case COMPONENT_ID -> aggregatedLogs.field(LOGS.COMPONENT_ID);
      case EVENT_ID -> aggregatedLogs.field(LOGS.EVENT_ID);
      case ORIGIN_ID -> aggregatedLogs.field(LOGS.ORIGIN_ID);
      case IP_ADDRESS -> aggregatedLogs.field(LOGS.IP_ADDRESS);
      case COURSE_ID -> aggregatedLogs.field(LOGS.COURSE_ID);
      case TIME_BUCKET -> aggregatedLogs.field(TIME_BUCKET, String.class);
      case SECTION_ID -> aggregatedLogs.field(MODULES.SECTION_ID);
    };
  }

  private Field<?> outerSelectFieldFor(
      CourseLogsSelectColumn column, Table<Record> aggregatedLogs) {
    return switch (column) {
      case USER_ID -> aggregatedLogs.field(LOGS.USER_ID);
      case USER_FULL_NAME -> USERS.FULL_NAME.as("userFullName");

      case MODULE_ID -> aggregatedLogs.field(LOGS.MODULE_ID);
      case MODULE_NAME -> MODULES.NAME.as("moduleName");

      case COMPONENT_ID -> aggregatedLogs.field(LOGS.COMPONENT_ID);
      case COMPONENT_NAME -> LOGS_COMPONENTS.NAME.as("componentName");

      case EVENT_ID -> aggregatedLogs.field(LOGS.EVENT_ID);
      case EVENT_NAME -> LOGS_EVENTS.NAME.as("eventName");

      case ORIGIN_ID -> aggregatedLogs.field(LOGS.ORIGIN_ID);
      case ORIGIN_NAME -> LOGS_ORIGINS.NAME.as("originName");

      case IP_ADDRESS -> aggregatedLogs.field(LOGS.IP_ADDRESS);
      case COURSE_ID -> aggregatedLogs.field(LOGS.COURSE_ID);
      case SECTION_ID -> aggregatedLogs.field(MODULES.SECTION_ID);

      case SECTION_NAME -> SECTIONS.NAME.as("sectionName");
    };
  }

  // =========================================================
  // CONDITIONS
  // =========================================================
  private List<Condition> buildConditions(CourseLogsStatisticsRequest request) {

    List<Condition> conditions = new ArrayList<>();

    if (request.getCourseId() != null) {
      conditions.add(LOGS.COURSE_ID.eq(request.getCourseId()));
    }

    var timeRange = request.getTimeRange();
    if (timeRange != null) {
      if (timeRange.getFrom() != null) {
        conditions.add(LOGS.LOGGED_AT.greaterOrEqual(timeRange.getFrom()));
      }
      if (timeRange.getTo() != null) {
        conditions.add(LOGS.LOGGED_AT.lessOrEqual(timeRange.getTo()));
      }
    }

    CourseLogsStatisticsRequestFilters filters = request.getFilters();
    if (filters != null) {
      addInFilter(conditions, LOGS.USER_ID, filters.getUserIds());
      addInFilter(conditions, LOGS.MODULE_ID, filters.getModuleIds());
      addInFilter(conditions, LOGS.COMPONENT_ID, filters.getComponentIds());
      addInFilter(conditions, LOGS.EVENT_ID, filters.getEventIds());
      addInFilter(conditions, LOGS.ORIGIN_ID, filters.getOriginIds());
      addInFilter(conditions, LOGS.IP_ADDRESS, filters.getIpAddresses());
      if (filters.getSectionIds() != null) {

        conditions.add(
            LOGS.MODULE_ID.in(
                DSL.select(MODULES.ID)
                    .from(MODULES)
                    .where(MODULES.SECTION_ID.in(filters.getSectionIds()))));
      }
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
}
