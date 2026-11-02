package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;

/** Writes course enrollments to tenant persistence. */
public interface SaveCourseEnrollmentsPort {

  /**
   * Replaces the stored enrollments of the course with the given ones, atomically.
   *
   * @param enrollments enrollments to persist
   */
  void save(CourseEnrollments enrollments);
}
