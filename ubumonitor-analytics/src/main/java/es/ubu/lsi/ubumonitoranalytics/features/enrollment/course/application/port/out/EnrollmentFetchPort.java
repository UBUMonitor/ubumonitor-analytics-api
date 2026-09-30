package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;

/** Fetches course enrollment data from Moodle. */
public interface EnrollmentFetchPort {

  /**
   * @param courseId Moodle course identifier
   * @param token Moodle web-service token
   * @return users enrolled in the course
   */
  UsersResponse fetchCourseEnrolledUsers(Integer courseId, String token);
}
