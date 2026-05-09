package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseContent;

public interface SyncCourseModulesUseCase {
    void syncCourseContent(CourseContent content);
}
