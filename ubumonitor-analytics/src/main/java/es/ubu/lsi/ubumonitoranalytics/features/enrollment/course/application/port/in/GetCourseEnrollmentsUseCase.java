package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;

/** Retrieves the users enrolled in a course from local persistence. */
public interface GetCourseEnrollmentsUseCase {

  /**
   * @param courseId Moodle course identifier
   * @return users and enrollment details for the course
   */
  UsersResponse getCourseEnrollment(Integer courseId);
}
