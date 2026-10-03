package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.FetchCourseLogsResult;

/** Queries persisted course logs using the supplied filters. */
public interface FetchLogPersistencePort {
  /**
   * @param courseLogsInfoRequest filters and pagination for log retrieval
   * @return the requested log page
   */
  FetchCourseLogsResult getLogs(CourseLogsInfoRequest courseLogsInfoRequest);
}
