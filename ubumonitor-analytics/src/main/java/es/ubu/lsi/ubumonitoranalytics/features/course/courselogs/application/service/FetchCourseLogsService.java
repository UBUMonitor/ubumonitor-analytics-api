package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.in.FetchCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.FetchLogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogsStatisticsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Retrieves course logs, statistics, and available log metadata. */
@Service
@RequiredArgsConstructor
public class FetchCourseLogsService implements FetchCourseLogsUseCase {

  private final FetchLogPersistencePort fetchLogPersistencePort;
  private final LogsStatisticsPort logsStatisticsPort;
  private final CourseLogsInfoPort courseLogsInfoPort;

  /**
   * @param courseLogsInfoRequest filters and pagination for log retrieval
   * @return the requested page of course logs
   */
  @Override
  @Transactional(readOnly = true)
  public FetchCourseLogsResult getCourseLogs(CourseLogsInfoRequest courseLogsInfoRequest) {
    return fetchLogPersistencePort.getLogs(courseLogsInfoRequest);
  }

  /**
   * @param request statistic filters and grouping options
   * @return calculated statistics
   */
  @Override
  @Transactional(readOnly = true)
  public CourseLogsStatisticsResult getCourseLogsStatistics(CourseLogsStatisticsRequest request) {
    return logsStatisticsPort.getCourseLogsStatistics(request);
  }

  /**
   * @param courseId Moodle course identifier
   * @return available log fields and filter values
   */
  @Override
  @Transactional(readOnly = true)
  public CourseLogsInfoResult getCourseLogsInfo(Long courseId) {
    return courseLogsInfoPort.getCourseLogsInfo(courseId);
  }
}
