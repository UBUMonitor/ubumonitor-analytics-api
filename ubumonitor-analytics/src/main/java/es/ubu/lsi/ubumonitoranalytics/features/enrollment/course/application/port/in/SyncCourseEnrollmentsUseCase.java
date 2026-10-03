package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;

/** Synchronizes users and enrollment details for a course from Moodle. */
public interface SyncCourseEnrollmentsUseCase {

  /**
   * @param courseId Moodle course identifier
   * @return the synchronized users and enrollment details
   */
  UsersResponse syncCourseEnrollments(Integer courseId);
}
