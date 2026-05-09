package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseContent;

public interface CourseContentPersistencePort {
    void save(CourseContent content);
}
