package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsResult;

/** Calculates aggregates over persisted course log records. */
public interface LogsStatisticsPort {
  /**
   * @param courseLogsStatisticsRequest statistic filters and grouping options
   * @return calculated statistics
   */
  CourseLogsStatisticsResult getCourseLogsStatistics(
      CourseLogsStatisticsRequest courseLogsStatisticsRequest);
}
