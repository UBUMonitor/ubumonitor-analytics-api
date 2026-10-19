package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;

/** Reads user identity and course enrollments from tenant persistence. */
public interface PersistenceFetchPort {
  /**
   * @param username Moodle username
   * @return the local user identifier, or {@code null} when absent
   */
  Long getUserIdByUserName(String username);

  /**
   * @param userId local user identifier
   * @return the user's enrolled courses
   */
  UserEnrolledCourses getEnrolledCourses(Long userId);
}
