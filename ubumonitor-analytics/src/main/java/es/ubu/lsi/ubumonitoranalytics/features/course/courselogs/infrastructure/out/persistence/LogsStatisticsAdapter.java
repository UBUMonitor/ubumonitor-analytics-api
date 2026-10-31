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
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Stream;
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

  /** Size of the SQL time buckets. 15 min fits every current IANA zone offset and DST shift. */
  private static final int BUCKET_SECONDS = 900;

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

    List<StatisticRow> rows;
    if (request.getInterval() == null) {
      rows = query.fetchInto(StatisticRow.class);
    } else {
      ZoneId zone = resolveTimeZone(request.getTimeZone());
      Field<Long> bucketField = aggregatedLogs.field(TIME_BUCKET, Long.class);
      List<OrderField<?>> orderFields = new ArrayList<>();
      orderFields.add(bucketField.asc());
      Set<Field<?>> stableOrderFields = new LinkedHashSet<>();
      for (CourseLogsSelectColumn field :
          request.getFields() == null ? List.<CourseLogsSelectColumn>of() : request.getFields()) {
        Field<?> groupField = aggregatedLogs.field(impliedIdFieldFor(field));
        // Fields not projected by the aggregation cannot be used as stable SQL tie-breakers.
        if (groupField != null) {
          stableOrderFields.add(groupField);
        }
      }
      for (Field<?> field : stableOrderFields) {
        orderFields.add(field.asc());
      }
      try (var stream = query.orderBy(orderFields).fetchStream()) {
        rows = processRecords(stream, request.getInterval(), zone);
      }
    }

    return CourseLogsStatisticsResult.builder().rows(rows).build();
  }

  static ZoneId resolveTimeZone(String timeZone) {
    if (timeZone == null) return ZoneOffset.UTC;
    try {
      return ZoneId.of(timeZone);
    } catch (DateTimeException exception) {
      throw new BadRequestException("Invalid timeZone: " + timeZone);
    }
  }

  static String formatTimeBucket(long bucket, TimeInterval interval, ZoneId zone) {
    var dateTime = Instant.ofEpochSecond(bucket).atZone(zone);
    return switch (interval) {
      case HOURLY ->
          dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00:00'", Locale.ROOT));
      case DAILY -> dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
      case MONTHLY -> dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT));
      case WEEKLY ->
          String.format(
              Locale.ROOT,
              "%04d-W%02d",
              dateTime.get(IsoFields.WEEK_BASED_YEAR),
              dateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
      case DAY_OF_WEEK -> Integer.toString(dateTime.getDayOfWeek().getValue());
    };
  }

  static List<StatisticRow> processRecords(
      Stream<Record> records, TimeInterval interval, ZoneId zone) {
    Map<StatisticRowKey, StatisticRow> mergedRows = new LinkedHashMap<>();
    records.forEach(
        record -> {
          Long bucket = record.get(TIME_BUCKET, Long.class);
          StatisticRow row = record.into(StatisticRow.class);
          row.setTimeBucket(formatTimeBucket(bucket, interval, zone));
          StatisticRowKey key = StatisticRowKey.from(row);
          StatisticRow existing = mergedRows.get(key);
          if (existing == null) {
            mergedRows.put(key, row);
          } else {
            existing.setValue(existing.getValue() + row.getValue());
          }
        });
    List<StatisticRow> rows = new ArrayList<>(mergedRows.values());
    if (interval == TimeInterval.DAY_OF_WEEK) {
      sortDayOfWeekRows(rows);
    }
    return rows;
  }

  static void sortDayOfWeekRows(List<StatisticRow> rows) {
    rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.getTimeBucket())));
  }

  private record StatisticRowKey(
      Long userId,
      String userFullName,
      Long moduleId,
      String moduleName,
      Short componentId,
      String componentName,
      Short eventId,
      String eventName,
      Byte originId,
      String originName,
      String ipAddress,
      Long courseId,
      String timeBucket,
      Long sectionId,
      String sectionName) {

    private static StatisticRowKey from(StatisticRow row) {
      return new StatisticRowKey(
          row.getUserId(),
          row.getUserFullName(),
          row.getModuleId(),
          row.getModuleName(),
          row.getComponentId(),
          row.getComponentName(),
          row.getEventId(),
          row.getEventName(),
          row.getOriginId(),
          row.getOriginName(),
          row.getIpAddress(),
          row.getCourseId(),
          row.getTimeBucket(),
          row.getSectionId(),
          row.getSectionName());
    }
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

    if (request.getFields() != null) {
      for (CourseLogsSelectColumn s : request.getFields()) {
        innerGroupBy.add(impliedIdFieldFor(s));
      }
    }

    if (request.getInterval() != null) {
      innerGroupBy.add(resolveIntervalField());
    }

    return innerGroupBy;
  }

  // =========================================================
  // Does this request need MODULES joined inside the inner
  // aggregation (because it groups/selects by SECTION)?
  // =========================================================
  private boolean needsSection(CourseLogsStatisticsRequest request) {

    boolean inFields =
        request.getFields() != null
            && request.getFields().stream().anyMatch(f -> f.name().contains("SECTION"));

    return inFields;
  }

  // =========================================================
  // OUTER SELECT
  // =========================================================
  private List<SelectField<?>> resolveOuterSelectFields(
      CourseLogsStatisticsRequest request, Table<Record> aggregatedLogs) {

    Set<SelectField<?>> fields = new LinkedHashSet<>();

    if (request.getInterval() != null) {
      fields.add(aggregatedLogs.field(TIME_BUCKET, Long.class));
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
  static Field<Long> resolveIntervalField() {
    return DSL.field(
            "(CAST({0} AS INTEGER) / {1}) * {1}",
            Long.class, LOGS.LOGGED_AT, DSL.inline(BUCKET_SECONDS))
        .as(TIME_BUCKET);
  }

  // =========================================================
  // FIELD MAPPING
  // =========================================================
  private Field<?> impliedIdFieldFor(CourseLogsSelectColumn column) {
    String columnName = column.name();
    if (columnName.contains("USER")) return LOGS.USER_ID;
    if (columnName.contains("MODULE")) return LOGS.MODULE_ID;
    if (columnName.contains("COMPONENT")) return LOGS.COMPONENT_ID;
    if (columnName.contains("EVENT")) return LOGS.EVENT_ID;
    if (columnName.contains("ORIGIN")) return LOGS.ORIGIN_ID;
    if (columnName.contains("IP_ADDRESS")) return LOGS.IP_ADDRESS;
    if (columnName.contains("SECTION")) return MODULES.SECTION_ID;

    throw new IllegalArgumentException("Unknown field: " + columnName);
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

    // Range limits remain UTC epoch seconds; clients should calculate them using their own zone.
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
