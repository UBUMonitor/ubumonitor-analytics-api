package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseContentApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseContentResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in.GetCourseContentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in.SyncCourseContentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CourseContentApiDelegateImpl implements CourseContentApiDelegate {

    private final SyncCourseContentUseCase syncCourseContentUseCase;
    private final GetCourseContentUseCase getCourseContentUseCase;
    private final CourseContentApiDelegateMapper courseContentApiDelegateMapper;

    @Override
    public ResponseEntity<CourseContentResponseDto> getCourseUsersContentInfo(Integer courseId) {
        return ResponseEntity.ok(courseContentApiDelegateMapper.toDto(getCourseContentUseCase.getCourseContent(courseId)));
    }

    @Override
    public ResponseEntity<CourseContentResponseDto> syncCourseContent(Integer courseId) {
        return ResponseEntity.ok(courseContentApiDelegateMapper.toDto(syncCourseContentUseCase.syncCourseContent(courseId)));
    }
}
