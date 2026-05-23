package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.GetCourseLogsCommand;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchCourseLogsResult;

public interface FetchLogPersistencePort {
    FetchCourseLogsResult getLogs(GetCourseLogsCommand getCourseLogsCommand);
}
