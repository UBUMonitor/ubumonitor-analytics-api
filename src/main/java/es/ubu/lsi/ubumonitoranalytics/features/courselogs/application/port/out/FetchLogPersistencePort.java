package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoRequest;

public interface FetchLogPersistencePort {
    FetchCourseLogsResult getLogs(CourseLogsInfoRequest courseLogsInfoRequest);
}
