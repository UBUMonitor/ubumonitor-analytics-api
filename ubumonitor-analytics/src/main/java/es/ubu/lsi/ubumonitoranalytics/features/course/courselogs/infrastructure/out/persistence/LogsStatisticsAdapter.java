package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sections.SECTIONS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogsStatisticsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsSelectColumn;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequestFilters;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequestTimeRange;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.StatisticRow;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.TimeInterval;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/**
 * Calculates requested course log aggregates with jOOQ.
 *
 * <p>The query has two levels. The inner one counts logs grouped by the id columns (and the time
 * bucket) so the GROUP BY runs over the narrow logs table. The outer one joins the lookup tables
 * only to resolve the requested names.
 */
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
    List<CourseLogsSelectColumn> fields = fieldsOf(request);

    Table<Record> aggregatedLogs = aggregateLogs(request, fields);

    SelectJoinStep<Record> query =
        jooq.dsl()
            .select(resolveOuterSelectFields(request, fields, aggregatedLogs))
            .from(aggregatedLogs);
    query = addOptimizedJoins(query, fields, aggregatedLogs);

    List<StatisticRow> rows =
        request.getInterval() == null
            ? query.fetchInto(StatisticRow.class)
            : fetchBucketed(query, request, fields, aggregatedLogs);

    return CourseLogsStatisticsResult.builder().rows(rows).build();
  }

  private static List<CourseLogsSelectColumn> fieldsOf(CourseLogsStatisticsRequest request) {
    return request.getFields() == null ? List.of() : request.getFields();
  }

  // =========================================================
  // INNER AGGREGATION
  // =========================================================

  private Table<Record> aggregateLogs(
      CourseLogsStatisticsRequest request, List<CourseLogsSelectColumn> fields) {
    Set<Field<?>> groupBy = resolveInnerGroupByFields(request, fields);

    List<SelectField<?>> select = new ArrayList<>(groupBy);
    select.add(DSL.count().as(VALUE));

    SelectJoinStep<Record> from = jooq.dsl().select(select).from(LOGS);

    // SECTION_ID lives in MODULES, so grouping by it needs the join inside the aggregation.
    if (needsSection(fields)) {
      from = from.leftJoin(MODULES).on(LOGS.MODULE_ID.eq(MODULES.ID));
    }

    return from.where(buildConditions(request)).groupBy(groupBy).asTable("agg_logs");
  }

  private Set<Field<?>> resolveInnerGroupByFields(
      CourseLogsStatisticsRequest request, List<CourseLogsSelectColumn> fields) {
    Set<Field<?>> groupBy = new LinkedHashSet<>();
    for (CourseLogsSelectColumn column : fields) {
      groupBy.add(impliedIdFieldFor(column));
    }
    if (request.getInterval() != null) {
      groupBy.add(resolveIntervalField());
    }
    return groupBy;
  }

  private static boolean needsSection(List<CourseLogsSelectColumn> fields) {
    return fields.contains(CourseLogsSelectColumn.SECTION_ID)
        || fields.contains(CourseLogsSelectColumn.SECTION_NAME);
  }

  /** The id column of the logs (or modules) table that each requested column is grouped by. */
  private static Field<?> impliedIdFieldFor(CourseLogsSelectColumn column) {
    return switch (column) {
      case USER_ID, USER_FULL_NAME -> LOGS.USER_ID;
      case MODULE_ID, MODULE_NAME -> LOGS.MODULE_ID;
      case COMPONENT_ID, COMPONENT_NAME -> LOGS.COMPONENT_ID;
      case EVENT_ID, EVENT_NAME -> LOGS.EVENT_ID;
      case ORIGIN_ID, ORIGIN_NAME -> LOGS.ORIGIN_ID;
      case IP_ADDRESS -> LOGS.IP_ADDRESS;
      case SECTION_ID, SECTION_NAME -> MODULES.SECTION_ID;
    };
  }

  static Field<Long> resolveIntervalField() {
    return DSL.field(
            "(CAST({0} AS INTEGER) / {1}) * {1}",
            Long.class, LOGS.LOGGED_AT, DSL.inline(BUCKET_SECONDS))
        .as(TIME_BUCKET);
  }

  // =========================================================
  // CONDITIONS
  // =========================================================

  private Condition buildConditions(CourseLogsStatisticsRequest request) {
    Condition condition = DSL.noCondition();

    if (request.getCourseId() != null) {
      condition = condition.and(LOGS.COURSE_ID.eq(request.getCourseId()));
    }

    // Range limits remain UTC epoch seconds; clients should calculate them using their own zone.
    CourseLogsStatisticsRequestTimeRange timeRange = request.getTimeRange();
    if (timeRange != null) {
      condition = condition.and(LogConditions.timeRange(timeRange.getFrom(), timeRange.getTo()));
    }

    CourseLogsStatisticsRequestFilters filters = request.getFilters();
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

  // =========================================================
  // OUTER SELECT AND JOINS
  // =========================================================

  private List<SelectField<?>> resolveOuterSelectFields(
      CourseLogsStatisticsRequest request,
      List<CourseLogsSelectColumn> fields,
      Table<Record> aggregatedLogs) {
    Set<SelectField<?>> select = new LinkedHashSet<>();

    if (request.getInterval() != null) {
      select.add(aggregatedLogs.field(TIME_BUCKET, Long.class));
    }
    for (CourseLogsSelectColumn column : fields) {
      select.add(outerSelectFieldFor(column, aggregatedLogs));
    }
    select.add(aggregatedLogs.field(VALUE, Integer.class));

    return new ArrayList<>(select);
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

  /** Joins the lookup tables only for the names that were requested. */
  private SelectJoinStep<Record> addOptimizedJoins(
      SelectJoinStep<Record> query,
      List<CourseLogsSelectColumn> fields,
      Table<Record> aggregatedLogs) {

    if (fields.contains(CourseLogsSelectColumn.USER_FULL_NAME)) {
      query = query.leftJoin(USERS).on(USERS.ID.eq(aggregatedLogs.field(LOGS.USER_ID)));
    }
    if (fields.contains(CourseLogsSelectColumn.MODULE_NAME)) {
      query = query.leftJoin(MODULES).on(MODULES.ID.eq(aggregatedLogs.field(LOGS.MODULE_ID)));
    }
    if (fields.contains(CourseLogsSelectColumn.COMPONENT_NAME)) {
      query =
          query
              .leftJoin(LOGS_COMPONENTS)
              .on(LOGS_COMPONENTS.ID.eq(aggregatedLogs.field(LOGS.COMPONENT_ID)));
    }
    if (fields.contains(CourseLogsSelectColumn.EVENT_NAME)) {
      query =
          query.leftJoin(LOGS_EVENTS).on(LOGS_EVENTS.ID.eq(aggregatedLogs.field(LOGS.EVENT_ID)));
    }
    if (fields.contains(CourseLogsSelectColumn.ORIGIN_NAME)) {
      query =
          query.leftJoin(LOGS_ORIGINS).on(LOGS_ORIGINS.ID.eq(aggregatedLogs.field(LOGS.ORIGIN_ID)));
    }
    // SECTION_ID is already projected by the inner aggregation (see aggregateLogs), so only the
    // SECTIONS join is left to resolve the human-readable name.
    if (fields.contains(CourseLogsSelectColumn.SECTION_NAME)) {
      query = query.leftJoin(SECTIONS).on(SECTIONS.ID.eq(aggregatedLogs.field(MODULES.SECTION_ID)));
    }

    return query;
  }

  // =========================================================
  // TIME BUCKETS
  // =========================================================

  private List<StatisticRow> fetchBucketed(
      SelectJoinStep<Record> query,
      CourseLogsStatisticsRequest request,
      List<CourseLogsSelectColumn> fields,
      Table<Record> aggregatedLogs) {
    ZoneId zone = resolveTimeZone(request.getTimeZone());
    List<OrderField<?>> orderBy = bucketOrderFields(fields, aggregatedLogs);

    try (var stream = query.orderBy(orderBy).fetchStream()) {
      return processRecords(stream, request.getInterval(), zone);
    }
  }

  /** Orders by bucket first, then by every projected id so the output is deterministic. */
  private static List<OrderField<?>> bucketOrderFields(
      List<CourseLogsSelectColumn> fields, Table<Record> aggregatedLogs) {
    List<OrderField<?>> orderBy = new ArrayList<>();
    orderBy.add(Objects.requireNonNull(aggregatedLogs.field(TIME_BUCKET, Long.class)).asc());

    Set<Field<?>> tieBreakers = new LinkedHashSet<>();
    for (CourseLogsSelectColumn column : fields) {
      Field<?> idField = aggregatedLogs.field(impliedIdFieldFor(column));
      // Fields not projected by the aggregation cannot be used as stable SQL tie-breakers.
      if (idField != null) {
        tieBreakers.add(idField);
      }
    }
    for (Field<?> field : tieBreakers) {
      orderBy.add(field.asc());
    }
    return orderBy;
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

  /**
   * Formats each 15-minute bucket in the requested zone and merges the rows that end up in the same
   * label (several buckets fall into one hour, day, week or month).
   */
  static List<StatisticRow> processRecords(
      Stream<Record> records, TimeInterval interval, ZoneId zone) {
    Map<StatisticRowKey, StatisticRow> mergedRows = new LinkedHashMap<>();
    Iterator<Record> iterator = records.iterator();
    while (iterator.hasNext()) {
      Record record = iterator.next();
      StatisticRow row = record.into(StatisticRow.class);
      row.setTimeBucket(formatTimeBucket(record.get(TIME_BUCKET, Long.class), interval, zone));

      StatisticRowKey key = StatisticRowKey.from(row);
      StatisticRow existing = mergedRows.get(key);
      if (existing == null) {
        mergedRows.put(key, row);
      } else {
        existing.setValue(existing.getValue() + row.getValue());
      }
    }
    List<StatisticRow> rows = new ArrayList<>(mergedRows.values());
    if (interval == TimeInterval.DAY_OF_WEEK) {
      sortDayOfWeekRows(rows);
    }
    return rows;
  }

  /** Sorts by ISO day of week (1 = Monday ... 7 = Sunday), keeping the order within each day. */
  static void sortDayOfWeekRows(List<StatisticRow> rows) {
    List<List<StatisticRow>> byDay = new ArrayList<>();
    for (int day = 0; day <= 7; day++) {
      byDay.add(new ArrayList<>());
    }
    for (StatisticRow row : rows) {
      byDay.get(Integer.parseInt(row.getTimeBucket())).add(row);
    }
    rows.clear();
    for (List<StatisticRow> dayRows : byDay) {
      rows.addAll(dayRows);
    }
  }

  /** Every {@link StatisticRow} dimension except the count, used to merge rows. */
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
}
