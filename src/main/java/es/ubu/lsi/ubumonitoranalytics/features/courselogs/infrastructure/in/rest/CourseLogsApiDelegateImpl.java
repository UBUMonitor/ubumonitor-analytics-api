package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseLogsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ImportCourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ProcessLogsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CourseLogsApiDelegateImpl implements CourseLogsApiDelegate {

    private final ImportCourseLogsUseCase importCourseLogsUseCase;
    private final CourseLogsApiDelegateMapper  courseLogsApiDelegateMapper;

    @Override
    public ResponseEntity<ImportCourseLogsResponseDto> importCourseLogs(Integer courseId, MultipartFile file) {
        ProcessLogsResult processLogsResult = importCourseLogsUseCase.process(courseId, file);
        return ResponseEntity.ok(courseLogsApiDelegateMapper.toDto(processLogsResult));
    }
}

