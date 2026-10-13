package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import java.util.List;

/** Persists role assignments for users in a course. */
public interface UserRolePersistencePort {

  /**
   * @param userIds identifiers of users participating in the synchronization
   * @param users users and their role data
   * @param courseId Moodle course identifier
   */
  void syncUserRoles(List<Integer> userIds, List<User> users, Integer courseId);
}
