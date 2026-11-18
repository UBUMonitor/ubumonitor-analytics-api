package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.RequiresOnlineSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Implements the generated REST operations for course enrollments. */
@Component
@RequiredArgsConstructor
public class CourseEnrollmentsApiDelegateImpl implements CourseEnrollmentsApiDelegate {

  private final SyncCourseEnrollmentsUseCase syncCourseEnrollmentsUseCase;
  private final GetCourseEnrollmentsUseCase getCourseEnrollmentsUseCase;

  private final CourseEnrollmentsApiDelegateMapper courseEnrollmentsApiDelegateMapper;
  private final CurrentSessionPort currentSessionContext;

  @Override
  @RequiresOnlineSession
  public ResponseEntity<CourseEnrollmentsResponseDto> syncCourseEnrollments(Long courseId) {
    return ResponseEntity.ok(
        toResponse(syncCourseEnrollmentsUseCase.syncCourseEnrollments(courseId)));
  }

  @Override
  public ResponseEntity<CourseEnrollmentsResponseDto> getCourseEnrollments(Long courseId) {
    return ResponseEntity.ok(
        toResponse(getCourseEnrollmentsUseCase.getCourseEnrollments(courseId)));
  }

  private CourseEnrollmentsResponseDto toResponse(CourseEnrollments enrollments) {
    return courseEnrollmentsApiDelegateMapper.toDto(
        enrollments, currentSessionContext.getSessionData());
  }
}
