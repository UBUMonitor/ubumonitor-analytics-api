package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;

public interface CourseContentPersistencePort {
    void save(CourseContent content);
}


