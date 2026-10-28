package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseActivityCompletionApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseActivityCompletionResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.in.CourseActivityCompletionUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Implements the generated REST operations for course content. */
@Component
@RequiredArgsConstructor
public class CourseActivityCompletionApiDelegateImpl
    implements CourseActivityCompletionApiDelegate {

  private final CourseActivityCompletionApiDelegateMapper courseActivityCompletionApiDelegateMapper;
  private final CourseActivityCompletionUseCase courseActivityCompletionUseCase;

  @Override
  public ResponseEntity<CourseActivityCompletionResponseDto> syncCourseActivityCompletion(
      Long courseId) {
    return ResponseEntity.ok(
        courseActivityCompletionApiDelegateMapper.toResponseDto(
            courseActivityCompletionUseCase.syncActivityCompletion(courseId)));
  }

  @Override
  public ResponseEntity<CourseActivityCompletionResponseDto> getCourseActivityCompletionInfo(
      Long courseId) {
    return ResponseEntity.ok(
        courseActivityCompletionApiDelegateMapper.toResponseDto(
            courseActivityCompletionUseCase.getActivityCompletion(courseId)));
  }

  @Override
  public ResponseEntity<CourseActivityCompletionResponseDto> getCourseActivityCompletionValues(
      Long courseId, List<Long> moduleIds, List<Long> userIds) {
    return ResponseEntity.ok(
        courseActivityCompletionApiDelegateMapper.toResponseDto(
            courseActivityCompletionUseCase.getActivityCompletion(courseId, moduleIds, userIds)));
  }
}
