package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseContentApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseContentResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in.GetCourseContentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in.SyncCourseContentUseCase;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.RequiresOnlineSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Implements the generated REST operations for course content. */
@Component
@RequiredArgsConstructor
public class CourseContentApiDelegateImpl implements CourseContentApiDelegate {

  private final SyncCourseContentUseCase syncCourseContentUseCase;
  private final GetCourseContentUseCase getCourseContentUseCase;
  private final CourseContentApiDelegateMapper courseContentApiDelegateMapper;

  @Override
  public ResponseEntity<CourseContentResponseDto> getCourseContent(Long courseId) {
    return ResponseEntity.ok(
        courseContentApiDelegateMapper.toDto(getCourseContentUseCase.getCourseContent(courseId)));
  }

  @Override
  @RequiresOnlineSession
  public ResponseEntity<CourseContentResponseDto> syncCourseContent(Long courseId) {
    return ResponseEntity.ok(
        courseContentApiDelegateMapper.toDto(syncCourseContentUseCase.syncCourseContent(courseId)));
  }
}
