package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;

/** Persists synchronized course content. */
public interface CourseContentPersistencePort {
  /**
   * @param content course sections and modules to persist
   */
  void save(CourseContent content);
}
