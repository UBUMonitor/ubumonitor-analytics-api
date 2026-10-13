package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;

/** Fetches a user's enrolled courses from Moodle. */
public interface EnrollmentCoursesApiFetchPort {
  /**
   * @param userId Moodle user identifier
   * @return the user's enrolled courses
   */
  UserEnrolledCourses fetchEnrolledCourses(Integer userId);
}
