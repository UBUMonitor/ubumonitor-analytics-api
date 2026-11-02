package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;

/** Reads course enrollments from tenant persistence. */
public interface LoadCourseEnrollmentsPort {

  /**
   * @param courseId Moodle course identifier
   * @return the active enrollments stored for the course; empty when there are none
   */
  CourseEnrollments load(Long courseId);
}
