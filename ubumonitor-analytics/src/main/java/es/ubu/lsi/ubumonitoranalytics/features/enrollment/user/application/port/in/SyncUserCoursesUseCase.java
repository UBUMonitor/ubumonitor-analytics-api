package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;

/** Synchronizes the current user's course enrollments from Moodle. */
public interface SyncUserCoursesUseCase {
  /**
   * @return the synchronized course enrollments
   */
  UserEnrolledCourses syncActualUserEnrollments();
}
