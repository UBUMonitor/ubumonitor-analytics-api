package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;

public interface SyncCourseContentUseCase {
  CourseContent syncCourseContent(Integer courseId);
}
