package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.FillGapStrategy;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.StatisticRow;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.TimeInterval;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Adds rows for missing buckets in continuous time series when gap filling is enabled. */
@Component
public class TimeSeriesFiller {

  /**
   * @param rows statistic rows returned by persistence
   * @param request interval, range, and gap-filling strategy
   * @return rows covering the requested continuous time range when gap filling applies
   */
  public List<StatisticRow> fill(List<StatisticRow> rows, CourseLogsStatisticsRequest request) {
    if (request.getInterval() == null
        || request.getTimeRange() == null
        || !request.getInterval().isContinuous()
        || request.getFillGapsStrategy() == FillGapStrategy.NONE) {
      return rows == null ? List.of() : rows;
    }

    Map<String, StatisticRow> indexed = new HashMap<>();
    if (rows != null) {
      for (StatisticRow row : rows) {
        if (row != null && row.getTimeBucket() != null) {
          indexed.putIfAbsent(row.getTimeBucket(), row);
        }
      }
    }

    List<String> fullRange =
        generateTimeBuckets(
            request.getTimeRange().getFrom(),
            request.getTimeRange().getTo(),
            request.getInterval());

    List<StatisticRow> result = new ArrayList<>(fullRange.size());
    Integer value = request.getFillGapsStrategy().getValue();
    for (String bucket : fullRange) {
      StatisticRow existing = indexed.get(bucket);
      if (existing != null) {
        result.add(existing);
      } else {
        result.add(StatisticRow.builder().timeBucket(bucket).value(value).build());
      }
    }

    return result;
  }

  private List<String> generateTimeBuckets(
      LocalDateTime from, LocalDateTime to, TimeInterval interval) {
    if (from == null || to == null || interval == null) {
      return List.of();
    }

    List<String> buckets = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(interval.pattern());
    TemporalUnit unit = interval.unit();
    LocalDateTime cursor = from;

    while (!cursor.isAfter(to)) {
      buckets.add(cursor.format(formatter));
      cursor = cursor.plus(1, unit);
    }

    return buckets;
  }
}
