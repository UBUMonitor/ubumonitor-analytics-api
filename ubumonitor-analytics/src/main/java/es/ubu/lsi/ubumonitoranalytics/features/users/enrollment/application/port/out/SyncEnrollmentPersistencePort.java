package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;

/** Stores synchronized course enrollment data for a user. */
public interface SyncEnrollmentPersistencePort {

  /**
   * @param userEnrolledCourses enrollment data to persist
   */
  void sync(UserEnrolledCourses userEnrolledCourses);
}
