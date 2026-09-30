package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;

/** Fetches course content from Moodle. */
public interface FetchCourseContentPort {
  /**
   * @param courseId Moodle course identifier
   * @return the course sections and modules returned by Moodle
   */
  CourseContent fetchCourseContent(Integer courseId);
}
