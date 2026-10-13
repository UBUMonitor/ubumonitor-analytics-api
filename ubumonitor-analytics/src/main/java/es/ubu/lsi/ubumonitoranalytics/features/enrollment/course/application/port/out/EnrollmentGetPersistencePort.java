package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;

/** Reads course enrollment data from tenant persistence. */
public interface EnrollmentGetPersistencePort {

  /**
   * @param courseId Moodle course identifier
   * @return users and enrollment details stored for the course
   */
  UsersResponse getCourseEnrollment(Integer courseId);
}
