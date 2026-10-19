package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;

/** Synchronizes the current user's course enrollments from Moodle. */
public interface SyncUserCoursesUseCase {
  /**
   * @return the synchronized course enrollments
   */
  UserEnrolledCourses syncActualUserEnrollments();
}
