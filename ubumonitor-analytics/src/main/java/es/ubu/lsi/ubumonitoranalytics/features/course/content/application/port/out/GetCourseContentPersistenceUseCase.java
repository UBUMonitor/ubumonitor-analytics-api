package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;

/** Reads course content from local persistence. */
public interface GetCourseContentPersistenceUseCase {

  /**
   * @param courseId Moodle course identifier
   * @return locally stored course sections and modules
   */
  CourseContent getCourseContent(Integer courseId);
}
