package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;

/** Synchronizes Moodle content for a course with local storage. */
public interface SyncCourseContentUseCase {
  /**
   * @param courseId Moodle course identifier
   * @return the synchronized course sections and modules
   */
  CourseContent syncCourseContent(Integer courseId);
}
