package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEnrollmentsApiDelegateImpl implements CourseEnrollmentsApiDelegate {

  private final SyncCourseEnrollmentsUseCase syncCourseEnrollmentsUseCase;
  private final GetCourseEnrollmentsUseCase getCourseEnrollmentsUseCase;
  private final CourseEnrollmentsApiDelegateMapper courseEnrollmentsApiDelegateMapper;
  private final CurrentSessionContext currentSessionContext;

  @Override
  public ResponseEntity<CourseEnrollmentsResponseDto> syncCourseUsersEnrollments(Integer courseId) {
    UsersResponse usersResponse = syncCourseEnrollmentsUseCase.syncCourseEnrollments(courseId);
    SessionData sessionData = currentSessionContext.getSessionData();
    return ResponseEntity.ok(
        courseEnrollmentsApiDelegateMapper.toDto(usersResponse, courseId, sessionData));
  }

  @Override
  public ResponseEntity<CourseEnrollmentsResponseDto> getCourseUsersEnrollmentsInfo(
      Integer courseId) {
    UsersResponse usersResponse = getCourseEnrollmentsUseCase.getCourseEnrollment(courseId);
    SessionData sessionData = currentSessionContext.getSessionData();

    return ResponseEntity.ok(
        courseEnrollmentsApiDelegateMapper.toDto(usersResponse, courseId, sessionData));
  }
}
