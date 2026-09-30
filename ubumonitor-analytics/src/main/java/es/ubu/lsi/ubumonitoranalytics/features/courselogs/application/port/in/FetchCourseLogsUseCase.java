package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsResult;

/** Retrieves course log records, metrics, and available filter information. */
public interface FetchCourseLogsUseCase {
  /**
   * @param getCourseLogsCommand filters and pagination for log retrieval
   * @return the requested log page
   */
  FetchCourseLogsResult getCourseLogs(CourseLogsInfoRequest getCourseLogsCommand);

  /**
   * @param courseLogsMetricsRequest filters and grouping for metric calculation
   * @return calculated course log metrics
   */
  CourseLogsMetricsResult getCourseLogsMetrics(CourseLogsMetricsRequest courseLogsMetricsRequest);

  /**
   * @param courseId Moodle course identifier
   * @return available log columns and filter values
   */
  CourseLogsInfoResult getCourseLogsInfo(Integer courseId);
}
