package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.FetchCourseLogsResult;

public interface FetchLogPersistencePort {
  FetchCourseLogsResult getLogs(CourseLogsInfoRequest courseLogsInfoRequest);
}
