package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.FetchCourseEnrollmentsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.SaveCourseEnrollmentsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Fetches Moodle course enrollments and persists them.
 *
 * <p>This service is deliberately not transactional: fetching from Moodle (including profile image
 * downloads) can be slow and must not hold a database connection. The save port is atomic on its
 * own.
 */
@Service
@RequiredArgsConstructor
public class SyncCourseEnrollmentsService implements SyncCourseEnrollmentsUseCase {

  private final FetchCourseEnrollmentsPort fetchCourseEnrollmentsPort;
  private final SaveCourseEnrollmentsPort saveCourseEnrollmentsPort;
  private final CurrentSessionPort currentSessionContext;

  @Override
  public CourseEnrollments syncCourseEnrollments(Long courseId) {
    String token = currentSessionContext.getSessionData().getMoodleToken();

    CourseEnrollments enrollments =
        fetchCourseEnrollmentsPort.fetchCourseEnrollments(courseId, token);
    saveCourseEnrollmentsPort.save(enrollments);

    return enrollments;
  }
}
