package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.UsersResponse;

/** Persists users and their course enrollment details. */
public interface UserCoursePersistencePort {

  /**
   * @param usersResponse users and enrollment data to persist
   */
  void sync(UsersResponse usersResponse);
}
