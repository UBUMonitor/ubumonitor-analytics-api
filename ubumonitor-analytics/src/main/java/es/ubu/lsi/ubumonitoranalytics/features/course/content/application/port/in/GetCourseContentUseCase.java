package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;

/** Retrieves locally stored content for a course. */
public interface GetCourseContentUseCase {
  /**
   * @param courseId Moodle course identifier
   * @return the course sections and modules
   */
  CourseContent getCourseContent(Integer courseId);
}
