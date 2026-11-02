package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;

/** Synchronizes the enrollments of a course from Moodle into local persistence. */
public interface SyncCourseEnrollmentsUseCase {

  /**
   * @param courseId Moodle course identifier
   * @return the enrollments fetched from Moodle and persisted
   */
  CourseEnrollments syncCourseEnrollments(Long courseId);
}
