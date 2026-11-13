package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.in.SyncUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out.EnrollmentCoursesApiFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out.PersistenceFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out.SyncEnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Fetches the current user's Moodle enrollments and synchronizes tenant persistence. */
@Service
@RequiredArgsConstructor
public class SyncUserCoursesService implements SyncUserCoursesUseCase {
  private final CurrentSessionPort currentSessionContext;
  private final EnrollmentCoursesApiFetchPort enrollmentCoursesApiFetchPort;
  private final SyncEnrollmentPersistencePort enrollmentPort;
  private final PersistenceFetchPort persistenceFetchPort;

  /**
   * @return synchronized course enrollments for the current user
   * @throws EntityNotFoundException when the current user is absent from local persistence
   */
  @Override
  public UserEnrolledCourses syncActualUserEnrollments() {
    SessionData sessionData = currentSessionContext.getSessionData();
    Long userId = sessionData.getUserId();

    if (userId == null) {
      throw new EntityNotFoundException("User not found " + sessionData.getUsername());
    }
    UserEnrolledCourses userEnrolledCourses =
        enrollmentCoursesApiFetchPort.fetchEnrolledCourses(userId);

    enrollmentPort.sync(userEnrolledCourses);

    return userEnrolledCourses;
  }
}
