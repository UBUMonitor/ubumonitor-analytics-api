package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseForumsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseForumsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.in.GetCourseForumsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.in.SyncCourseForumsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseForumsApiDelegateImpl implements CourseForumsApiDelegate {

  private final SyncCourseForumsUseCase syncCourseForumsUseCase;
  private final GetCourseForumsUseCase getCourseForumsUseCase;
  private final CourseForumsApiDelegateMapper mapper;

  @Override
  public ResponseEntity<CourseForumsResponseDto> syncCourseForums(Long courseId) {
    return ResponseEntity.ok(mapper.toDto(syncCourseForumsUseCase.syncCourseForums(courseId)));
  }

  @Override
  public ResponseEntity<CourseForumsResponseDto> getCourseForums(Long courseId) {
    return ResponseEntity.ok(mapper.toDto(getCourseForumsUseCase.getCourseForums(courseId)));
  }
}
