package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service.TimeSeriesFiller;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequestTimeRange;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.FillGapStrategy;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.StatisticRow;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.TimeInterval;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class TimeSeriesFillerTest {

  private final TimeSeriesFiller filler = new TimeSeriesFiller();

  @Test
  void fillsEveryMissingDailyBucketInTheRequestedRange() {
    CourseLogsStatisticsRequest request =
        requestFor(LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 3, 0, 0));

    List<StatisticRow> rows =
        List.of(
            StatisticRow.builder().timeBucket("2026-10-01").value(3).build(),
            StatisticRow.builder().timeBucket("2026-10-03").value(7).build());

    List<StatisticRow> filled = filler.fill(rows, request);

    assertIterableEquals(
        List.of("2026-10-01", "2026-10-02", "2026-10-03"),
        filled.stream().map(StatisticRow::getTimeBucket).toList());
    assertEquals(3, filled.get(0).getValue());
    assertEquals(0, filled.get(1).getValue());
    assertEquals(7, filled.get(2).getValue());
  }

  @Test
  void returnsOriginalRowsWhenGapFillingIsDisabled() {
    CourseLogsStatisticsRequest request =
        requestFor(LocalDateTime.of(2026, 10, 1, 0, 0), LocalDateTime.of(2026, 10, 3, 0, 0));
    request.setFillGapsStrategy(FillGapStrategy.NONE);

    List<StatisticRow> rows =
        List.of(
            StatisticRow.builder().timeBucket("2026-10-01").value(3).build(),
            StatisticRow.builder().timeBucket("2026-10-03").value(7).build());

    assertEquals(rows, filler.fill(rows, request));
  }

  private CourseLogsStatisticsRequest requestFor(LocalDateTime from, LocalDateTime to) {
    CourseLogsStatisticsRequest request = new CourseLogsStatisticsRequest();
    request.setInterval(TimeInterval.DAILY);
    request.setTimeRange(new CourseLogsStatisticsRequestTimeRange());
    request.getTimeRange().setFrom(from);
    request.getTimeRange().setTo(to);
    request.setFillGapsStrategy(FillGapStrategy.ZERO);
    return request;
  }
}
