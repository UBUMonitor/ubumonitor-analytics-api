package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence.LogsStatisticsAdapter;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

class LogsStatisticsAdapterTest {

  @Test
  void dayOfWeekBucketDefaultsToMonday() throws Exception {
    try (var connection =
        DriverManager.getConnection("jdbc:h2:mem:week-start-default-test", "sa", "")) {
      DSLContext dsl = DSL.using(connection);

      Integer bucket =
          dsl.select(
                  LogsStatisticsAdapter.dayOfWeekField(
                      DSL.val(LocalDateTime.of(2026, 10, 19, 12, 0))))
              .fetchOne(0, Integer.class);

      assertEquals(1, bucket);
    }
  }

  @Test
  void dayOfWeekBucketUsesMondayAsWeekStart() throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:week-start-test", "sa", "")) {
      DSLContext dsl = DSL.using(connection);

      Integer bucket =
          dsl.select(
                  LogsStatisticsAdapter.dayOfWeekField(
                      DSL.val(LocalDateTime.of(2026, 10, 18, 12, 0))))
              .fetchOne(0, Integer.class);

      assertEquals(7, bucket);
    }
  }
}
