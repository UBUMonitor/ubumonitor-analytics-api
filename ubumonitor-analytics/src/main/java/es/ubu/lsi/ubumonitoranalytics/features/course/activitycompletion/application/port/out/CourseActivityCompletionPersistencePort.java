package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;

/** Persists synchronized course content. */
public interface CourseActivityCompletionPersistencePort {

  void save(ActivityCompletionResult activityCompletionResult);
}
