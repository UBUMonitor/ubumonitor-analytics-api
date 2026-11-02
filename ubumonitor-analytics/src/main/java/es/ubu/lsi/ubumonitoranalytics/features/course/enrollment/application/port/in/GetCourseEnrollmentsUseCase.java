package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;

/** Retrieves the users enrolled in a course from local persistence. */
public interface GetCourseEnrollmentsUseCase {

  /**
   * @param courseId Moodle course identifier
   * @return the enrollments stored for the course
   */
  CourseEnrollments getCourseEnrollments(Long courseId);
}
