package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;

/** Fetches course enrollments from Moodle. */
public interface FetchCourseEnrollmentsPort {

  /**
   * @param courseId Moodle course identifier
   * @param token Moodle web-service token
   * @return the enrollments of the course, including profile pictures
   */
  CourseEnrollments fetchCourseEnrollments(Long courseId, String token);
}
