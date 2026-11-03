package es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;

public interface SyncCourseForumsUseCase {
  CourseForums syncCourseForums(Long courseId);
}
