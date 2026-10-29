package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence.LogsStatisticsAdapter;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.nio.file.Path;
import java.time.Instant;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LogsStatisticsAdapterTest {

  @TempDir Path tempDir;

  @Test
  void dayOfWeekBucketDefaultsToMonday() throws Exception {
    try (var connection = createConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);

      Integer bucket =
          dsl.select(
                  LogsStatisticsAdapter.dayOfWeekField(
                      DSL.val(Instant.parse("2026-10-19T12:00:00Z").getEpochSecond())))
              .fetchOne(0, Integer.class);

      assertEquals(1, bucket);
    }
  }

  @Test
  void dayOfWeekBucketUsesMondayAsWeekStart() throws Exception {
    try (var connection = createConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);

      Integer bucket =
          dsl.select(
                  LogsStatisticsAdapter.dayOfWeekField(
                      DSL.val(Instant.parse("2026-10-18T12:00:00Z").getEpochSecond())))
              .fetchOne(0, Integer.class);

      assertEquals(7, bucket);
    }
  }

  @Test
  void weeklyBucketUsesIsoWeeksStartingOnMonday() throws Exception {
    try (var connection = createConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);

      String sunday =
          dsl.select(
                  LogsStatisticsAdapter.weekBucketField(
                      DSL.val(Instant.parse("2021-01-03T12:00:00Z").getEpochSecond())))
              .fetchOne(0, String.class);
      String monday =
          dsl.select(
                  LogsStatisticsAdapter.weekBucketField(
                      DSL.val(Instant.parse("2021-01-04T12:00:00Z").getEpochSecond())))
              .fetchOne(0, String.class);

      assertEquals("2020-W53", sunday);
      assertEquals("2021-W01", monday);
    }
  }

  private java.sql.Connection createConnection() throws Exception {
    String jdbcUrl =
        "jdbc:sqlite:"
            + tempDir.resolve("logs-statistics.db").toAbsolutePath().toString().replace("\\", "/");
    return DatabaseUtil.createDataSource(jdbcUrl, "test-key").getConnection();
  }
}
