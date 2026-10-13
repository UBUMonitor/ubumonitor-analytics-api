package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;

/** Stores synchronized course enrollment data for a user. */
public interface SyncEnrollmentPersistencePort {

  /**
   * @param userEnrolledCourses enrollment data to persist
   */
  void sync(UserEnrolledCourses userEnrolledCourses);
}
