package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsResult;

public interface LogsMetricsPort {
    CourseLogsMetricsResult getCourseLogsMetrics(CourseLogsMetricsRequest courseLogsMetricsRequest);
}
