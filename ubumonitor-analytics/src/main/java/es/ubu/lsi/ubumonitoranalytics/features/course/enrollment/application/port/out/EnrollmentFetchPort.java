package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.UsersResponse;

/** Fetches course enrollment data from Moodle. */
public interface EnrollmentFetchPort {

  /**
   * @param courseId Moodle course identifier
   * @param token Moodle web-service token
   * @return users enrolled in the course
   */
  UsersResponse fetchCourseEnrolledUsers(Long courseId, String token);
}
