package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoResult;

/** Reads available log columns and filter values for a course. */
public interface CourseLogsInfoPort {
  /**
   * @param courseId Moodle course identifier
   * @return log metadata for the course
   */
  CourseLogsInfoResult getCourseLogsInfo(Integer courseId);
}
