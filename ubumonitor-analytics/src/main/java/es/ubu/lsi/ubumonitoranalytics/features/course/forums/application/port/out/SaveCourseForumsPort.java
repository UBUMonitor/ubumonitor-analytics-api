package es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;

public interface SaveCourseForumsPort {
  void save(CourseForums courseForums);
}
