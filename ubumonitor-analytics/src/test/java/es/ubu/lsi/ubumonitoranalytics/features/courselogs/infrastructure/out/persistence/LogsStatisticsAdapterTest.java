package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.DriverManager;
import java.time.LocalDateTime;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence.LogsStatisticsAdapter;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LogsStatisticsAdapterTest {

  @Test
  void dayOfWeekBucketDefaultsToMonday() throws Exception {
    try (var connection =
        DriverManager.getConnection("jdbc:h2:mem:week-start-default-test", "sa", "")) {
      DSLContext dsl = DSL.using(connection);

      Integer bucket =
          dsl.select(
                  LogsStatisticsAdapter.dayOfWeekField(
                      DSL.val(LocalDateTime.of(2026, 10, 19, 12, 0)), null))
              .fetchOne(0, Integer.class);

      assertEquals(1, bucket);
    }
  }

  @ParameterizedTest
  @CsvSource({"1, 7", "2, 6", "3, 5", "4, 4", "5, 3", "6, 2", "7, 1"})
  void dayOfWeekBucketUsesConfiguredWeekStart(int weekStartsOn, int expectedBucket)
      throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:week-start-test", "sa", "")) {
      DSLContext dsl = DSL.using(connection);

      Integer bucket =
          dsl.select(
                  LogsStatisticsAdapter.dayOfWeekField(
                      DSL.val(LocalDateTime.of(2026, 10, 18, 12, 0)), weekStartsOn))
              .fetchOne(0, Integer.class);

      assertEquals(expectedBucket, bucket);
    }
  }
}
