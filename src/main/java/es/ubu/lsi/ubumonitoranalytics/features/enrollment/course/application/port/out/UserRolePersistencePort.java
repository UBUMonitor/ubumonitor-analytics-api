package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;

import java.util.List;

public interface UserRolePersistencePort {

    void syncUserRoles(List<Integer> userIds, List<User> users, Integer courseId);
}
