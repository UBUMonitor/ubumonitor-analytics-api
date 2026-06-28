package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;

public interface PersistenceFetchPort {
  Integer getUserIdByUserName(String username);

  UserEnrolledCourses getEnrolledCourses(Integer userId);
}
