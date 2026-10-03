package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;

/** Reads user identity and course enrollments from tenant persistence. */
public interface PersistenceFetchPort {
  /**
   * @param username Moodle username
   * @return the local user identifier, or {@code null} when absent
   */
  Integer getUserIdByUserName(String username);

  /**
   * @param userId local user identifier
   * @return the user's enrolled courses
   */
  UserEnrolledCourses getEnrolledCourses(Integer userId);
}
