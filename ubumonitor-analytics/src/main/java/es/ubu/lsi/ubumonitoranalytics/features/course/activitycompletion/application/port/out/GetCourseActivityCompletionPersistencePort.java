package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import java.util.List;

/** Reads course content from local persistence. */
public interface GetCourseActivityCompletionPersistencePort {

  /**
   * @param courseId Moodle course identifier
   * @return locally stored course sections and modules
   */
  ActivityCompletionResult getCourseActivitiesCompletions(Long courseId);

  ActivityCompletionResult getCourseActivitiesCompletions(
      Long courseId, List<Long> modulesIds, List<Long> userIds);
}
