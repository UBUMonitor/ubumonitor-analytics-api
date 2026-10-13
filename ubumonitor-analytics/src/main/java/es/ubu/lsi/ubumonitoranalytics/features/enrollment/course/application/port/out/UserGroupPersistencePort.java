package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import java.util.List;

/** Persists group memberships for users in a course. */
public interface UserGroupPersistencePort {
  /**
   * @param userIds identifiers of users participating in the synchronization
   * @param users users and their group data
   * @param courseId Moodle course identifier
   */
  void syncUserGroups(List<Integer> userIds, List<User> users, Integer courseId);
}
