package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import java.util.List;

public interface UserGroupPersistencePort {
  void syncUserGroups(List<Integer> userIds, List<User> users, Integer courseId);
}
