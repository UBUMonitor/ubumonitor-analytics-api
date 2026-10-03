package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.EnrollmentGetPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
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
