package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.FetchCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.FetchLogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.GetCourseLogsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FetchCourseLogsService implements FetchCourseLogsUseCase {

    private final FetchLogPersistencePort fetchLogPersistencePort;



    @Override
    public FetchCourseLogsResult getCourseLogs(GetCourseLogsCommand getCourseLogsCommand) {
        return fetchLogPersistencePort.getLogs(getCourseLogsCommand);
    }
}
