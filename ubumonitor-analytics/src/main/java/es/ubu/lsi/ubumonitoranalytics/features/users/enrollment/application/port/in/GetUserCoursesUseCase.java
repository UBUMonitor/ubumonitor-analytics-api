package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;

/** Retrieves the current user's enrolled courses. */
public interface GetUserCoursesUseCase {
  /**
   * @return the current user's enrolled courses
   */
  UserEnrolledCourses getActualUserEnrollments();
}
