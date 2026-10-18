package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.EnrollmentGetPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.UsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Retrieves persisted enrollment data for a course. */
@Service
@RequiredArgsConstructor
public class GetCourseEnrollmentsService implements GetCourseEnrollmentsUseCase {

  private final EnrollmentGetPersistencePort enrollmentGetPersistencePort;

  /**
   * @param courseId Moodle course identifier
   * @return users and enrollment details stored for the course
   */
  @Override
  @Transactional(readOnly = true)
  public UsersResponse getCourseEnrollment(Integer courseId) {
    return enrollmentGetPersistencePort.getCourseEnrollment(courseId);
  }
}
