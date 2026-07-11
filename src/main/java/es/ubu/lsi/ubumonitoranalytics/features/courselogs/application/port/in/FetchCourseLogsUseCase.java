package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsResult;

public interface FetchCourseLogsUseCase {
  FetchCourseLogsResult getCourseLogs(CourseLogsInfoRequest getCourseLogsCommand);

  CourseLogsMetricsResult getCourseLogsMetrics(CourseLogsMetricsRequest courseLogsMetricsRequest);
}
