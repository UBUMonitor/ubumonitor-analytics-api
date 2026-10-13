package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsResult;

/** Calculates aggregates over persisted course log records. */
public interface LogsMetricsPort {
  /**
   * @param courseLogsMetricsRequest metric filters and grouping options
   * @return calculated metrics
   */
  CourseLogsMetricsResult getCourseLogsMetrics(CourseLogsMetricsRequest courseLogsMetricsRequest);
}
