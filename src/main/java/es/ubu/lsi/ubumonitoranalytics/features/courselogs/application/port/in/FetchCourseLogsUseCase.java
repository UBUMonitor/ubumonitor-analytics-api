package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.GetCourseLogsCommand;

public interface FetchCourseLogsUseCase {
    FetchCourseLogsResult getCourseLogs(GetCourseLogsCommand getCourseLogsCommand);
}
