package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsResult;

/** Retrieves course log records, statistics, and available filter information. */
public interface FetchCourseLogsUseCase {
  /**
   * @param getCourseLogsCommand filters and pagination for log retrieval
   * @return the requested log page
   */
  FetchCourseLogsResult getCourseLogs(CourseLogsInfoRequest getCourseLogsCommand);

  /**
   * @param courseLogsStatisticsRequest filters and grouping for statistic calculation
   * @return calculated course log statistics
   */
  CourseLogsStatisticsResult getCourseLogsStatistics(
      CourseLogsStatisticsRequest courseLogsStatisticsRequest);

  /**
   * @param courseId Moodle course identifier
   * @return available log columns and filter values
   */
  CourseLogsInfoResult getCourseLogsInfo(Long courseId);
}
