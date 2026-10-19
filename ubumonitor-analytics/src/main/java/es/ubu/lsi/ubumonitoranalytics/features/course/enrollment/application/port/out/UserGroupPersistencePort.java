package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import java.util.List;

/** Persists group memberships for users in a course. */
public interface UserGroupPersistencePort {
  /**
   * @param userIds identifiers of users participating in the synchronization
   * @param users users and their group data
   * @param courseId Moodle course identifier
   */
  void syncUserGroups(List<Long> userIds, List<User> users, Long courseId);
}
