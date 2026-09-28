package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.EnrollmentFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncCourseEnrollmentsService implements SyncCourseEnrollmentsUseCase {

  private final EnrollmentFetchPort enrollmentFetchPort;
  private final CurrentSessionContext currentSessionContext;

  private final UserCoursePersistencePort userCoursePersistencePort;

  @Override
  @Transactional
  public UsersResponse syncCourseEnrollments(Integer courseId) {
    UsersResponse usersResponse =
        enrollmentFetchPort.fetchCourseEnrolledUsers(
            courseId, currentSessionContext.getSessionData().getMoodleToken());

    userCoursePersistencePort.sync(usersResponse);
    return usersResponse;
  }
}
