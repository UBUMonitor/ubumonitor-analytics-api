package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseAssignmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseAssignmentSubmissionsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseAssignmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.in.CourseAssignmentSubmissionsUseCase;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.RequiresOnlineSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseAssignmentSubmissionsApiDelegateImpl implements CourseAssignmentsApiDelegate {

  private final CourseAssignmentSubmissionsApiDelegateMapper mapper;
  private final CourseAssignmentSubmissionsUseCase useCase;

  @Override
  @RequiresOnlineSession
  public ResponseEntity<CourseAssignmentsResponseDto> syncCourseAssignmentSubmissions(
      Long courseId) {
    return ResponseEntity.ok(mapper.toAssignmentsResponse(useCase.sync(courseId)));
  }

  @Override
  public ResponseEntity<CourseAssignmentsResponseDto> getCourseAssignmentSubmissions(
      Long courseId) {
    return ResponseEntity.ok(mapper.toAssignmentsResponse(useCase.get(courseId)));
  }

  @Override
  public ResponseEntity<CourseAssignmentSubmissionsResponseDto> getCourseAssignmentSubmissionValues(
      Long courseId, List<Long> assignmentIds, List<Long> userIds) {
    return ResponseEntity.ok(
        mapper.toSubmissionsResponse(useCase.get(courseId, assignmentIds, userIds)));
  }
}
