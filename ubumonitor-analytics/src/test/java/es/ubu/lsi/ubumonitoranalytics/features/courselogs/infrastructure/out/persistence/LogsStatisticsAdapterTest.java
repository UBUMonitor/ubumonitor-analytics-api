package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.StatisticRow;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.TimeInterval;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

class LogsStatisticsAdapterTest {

  @Test
  void dailyBucketsUseRequestedZone() {
    long bucket = Instant.parse("2026-10-30T23:30:00Z").getEpochSecond();

    assertEquals(
        "2026-10-30",
        LogsStatisticsAdapter.formatTimeBucket(bucket, TimeInterval.DAILY, ZoneId.of("UTC")));
    assertEquals(
        "2026-10-31",
        LogsStatisticsAdapter.formatTimeBucket(
            bucket, TimeInterval.DAILY, ZoneId.of("Europe/Madrid")));
  }

  @Test
  void repeatedHourlyBucketIsMergedDuringAutumnDstTransition() {
    long summerTimeHour = Instant.parse("2026-10-25T00:30:00Z").getEpochSecond();
    long standardTimeHour = Instant.parse("2026-10-25T01:30:00Z").getEpochSecond();
    ZoneId madrid = ZoneId.of("Europe/Madrid");
    String repeatedLabel =
        LogsStatisticsAdapter.formatTimeBucket(summerTimeHour, TimeInterval.HOURLY, madrid);
    String secondLabel =
        LogsStatisticsAdapter.formatTimeBucket(standardTimeHour, TimeInterval.HOURLY, madrid);

    assertEquals("2026-10-25T02:00:00", repeatedLabel);
    assertEquals(repeatedLabel, secondLabel);
    assertEquals(
        2,
        LogsStatisticsAdapter.processRecords(
                Stream.of(
                    record(Instant.parse("2026-10-25T00:30:00Z").getEpochSecond(), 13L, 1),
                    record(Instant.parse("2026-10-25T01:30:00Z").getEpochSecond(), 13L, 1)),
                TimeInterval.HOURLY,
                madrid)
            .get(0)
            .getValue());
  }

  @Test
  void weeklyBucketsUseIsoWeekYear() {
    long newYearsDay = Instant.parse("2027-01-01T12:00:00Z").getEpochSecond();
    long followingMonday = Instant.parse("2027-01-04T12:00:00Z").getEpochSecond();

    assertEquals(
        "2026-W53",
        LogsStatisticsAdapter.formatTimeBucket(newYearsDay, TimeInterval.WEEKLY, ZoneId.of("UTC")));
    assertEquals(
        "2027-W01",
        LogsStatisticsAdapter.formatTimeBucket(
            followingMonday, TimeInterval.WEEKLY, ZoneId.of("UTC")));
  }

  @Test
  void dayOfWeekIsMondayBasedInRequestedZone() {
    long mondayInUtcButTuesdayInMadrid = Instant.parse("2026-10-26T23:30:00Z").getEpochSecond();

    assertEquals(
        "1",
        LogsStatisticsAdapter.formatTimeBucket(
            mondayInUtcButTuesdayInMadrid, TimeInterval.DAY_OF_WEEK, ZoneId.of("UTC")));
    assertEquals(
        "2",
        LogsStatisticsAdapter.formatTimeBucket(
            mondayInUtcButTuesdayInMadrid, TimeInterval.DAY_OF_WEEK, ZoneId.of("Europe/Madrid")));
  }

  @Test
  void invalidTimeZoneProducesBadRequest() {
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> LogsStatisticsAdapter.resolveTimeZone("Nope/Zone"));

    assertEquals("Invalid timeZone: Nope/Zone", exception.getMessage());
  }

  @Test
  void acceptsFractionalHourZones() {
    for (String timeZone :
        List.of("Asia/Kolkata", "Asia/Kathmandu", "Europe/Madrid", "America/New_York", "UTC")) {
      assertEquals(ZoneId.of(timeZone), LogsStatisticsAdapter.resolveTimeZone(timeZone));
    }
    assertEquals(ZoneOffset.UTC, LogsStatisticsAdapter.resolveTimeZone(null));
  }

  @Test
  void intervalSqlUsesInline900SecondIntegerBuckets() {
    DSLContext dsl = DSL.using(SQLDialect.SQLITE);

    String sql = dsl.renderInlined(DSL.select(LogsStatisticsAdapter.resolveIntervalField()));

    assertFalse(sql.contains("?"));
    assertTrue(sql.contains("CAST("));
    assertTrue(sql.contains(" / 900) * 900"));
  }

  @Test
  void dailyBucketsHandleIndiaAndNepalOffsetBoundaries() {
    ZoneId kolkata = ZoneId.of("Asia/Kolkata");
    ZoneId kathmandu = ZoneId.of("Asia/Kathmandu");

    assertEquals("2026-10-31", dayBucket("2026-10-30T18:30:00Z", kolkata));
    assertEquals("2026-10-30", dayBucket("2026-10-30T18:29:00Z", kolkata));
    assertEquals("2026-10-31", dayBucket("2026-10-30T18:15:00Z", kathmandu));
    assertEquals("2026-10-30", dayBucket("2026-10-30T18:14:00Z", kathmandu));
  }

  @Test
  void hourlyBucketsWithIndiaOffsetAreGroupedByLocalHour() {
    List<StatisticRow> rows =
        LogsStatisticsAdapter.processRecords(
            Stream.of(
                record(bucketStart("2026-10-30T07:10:00Z"), 13L, 1),
                record(bucketStart("2026-10-30T07:40:00Z"), 13L, 1)),
            TimeInterval.HOURLY,
            ZoneId.of("Asia/Kolkata"));

    assertEquals(
        List.of("2026-10-30T12:00:00", "2026-10-30T13:00:00"),
        rows.stream().map(StatisticRow::getTimeBucket).toList());
    assertEquals(List.of(1, 1), rows.stream().map(StatisticRow::getValue).toList());
  }

  @Test
  void dailyBucketsMergeByUserAndRemainChronologicalAcrossMonthBoundary() {
    ZoneId madrid = ZoneId.of("Europe/Madrid");
    List<StatisticRow> merged =
        LogsStatisticsAdapter.processRecords(
            Stream.of(
                record(Instant.parse("2026-10-30T23:30:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-10-30T23:45:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-10-30T23:30:00Z").getEpochSecond(), 14L, 1),
                record(Instant.parse("2026-11-01T00:30:00Z").getEpochSecond(), 13L, 1)),
            TimeInterval.DAILY,
            madrid);

    assertEquals(
        List.of("2026-10-31", "2026-10-31", "2026-11-01"),
        merged.stream().map(StatisticRow::getTimeBucket).toList());
    assertEquals(List.of(13L, 14L, 13L), merged.stream().map(StatisticRow::getUserId).toList());
    assertEquals(2, merged.get(0).getValue());
  }

  @Test
  void weeklyBucketsUseIsoWeekYearForYearBoundaryLogs() {
    List<StatisticRow> rows =
        LogsStatisticsAdapter.processRecords(
            Stream.of(
                record(Instant.parse("2026-12-31T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2027-01-01T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2027-01-04T12:00:00Z").getEpochSecond(), 13L, 1)),
            TimeInterval.WEEKLY,
            ZoneId.of("UTC"));

    assertEquals(
        List.of("2026-W53", "2027-W01"), rows.stream().map(StatisticRow::getTimeBucket).toList());
    assertEquals(List.of(2, 1), rows.stream().map(StatisticRow::getValue).toList());
  }

  @Test
  void dayOfWeekRowsAreSortedMondayThroughSunday() {
    List<StatisticRow> rows =
        LogsStatisticsAdapter.processRecords(
            Stream.of(
                record(Instant.parse("2026-10-28T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-10-29T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-10-30T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-10-31T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-11-01T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-11-02T12:00:00Z").getEpochSecond(), 13L, 1),
                record(Instant.parse("2026-11-03T12:00:00Z").getEpochSecond(), 13L, 1)),
            TimeInterval.DAY_OF_WEEK,
            ZoneId.of("UTC"));

    assertEquals(
        List.of("1", "2", "3", "4", "5", "6", "7"),
        rows.stream().map(StatisticRow::getTimeBucket).toList());
  }

  private static Record record(long bucket, long userId, int value) {
    DSLContext dsl = DSL.using(SQLDialect.SQLITE);
    var bucketField = DSL.field(LogsStatisticsAdapter.TIME_BUCKET, Long.class);
    var userIdField = DSL.field("userId", Long.class);
    var valueField = DSL.field(LogsStatisticsAdapter.VALUE, Integer.class);
    Record record = dsl.newRecord(bucketField, userIdField, valueField);
    record.set(bucketField, bucket);
    record.set(userIdField, userId);
    record.set(valueField, value);
    return record;
  }

  private static String dayBucket(String timestamp, ZoneId zone) {
    List<StatisticRow> rows =
        LogsStatisticsAdapter.processRecords(
            Stream.of(record(bucketStart(timestamp), 13L, 1)), TimeInterval.DAILY, zone);
    return rows.get(0).getTimeBucket();
  }

  private static long bucketStart(String timestamp) {
    long epochSecond = Instant.parse(timestamp).getEpochSecond();
    return Math.floorDiv(epochSecond, 900) * 900;
  }
}
