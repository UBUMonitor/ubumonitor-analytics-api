package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogsResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImportCourseLogsUseCase {

    ProcessLogsResult process(Integer course, MultipartFile file);

    ProcessLogsResult sync(Integer course);
}
