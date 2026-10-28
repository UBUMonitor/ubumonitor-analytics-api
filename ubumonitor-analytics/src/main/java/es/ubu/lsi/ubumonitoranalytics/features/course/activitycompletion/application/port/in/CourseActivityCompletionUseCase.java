package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import java.util.List;

/** Synchronizes Moodle content for a course with local storage. */
public interface CourseActivityCompletionUseCase {
  /**
   * @param courseId Moodle course identifier
   * @return the synchronized course sections and modules
   */
  ActivityCompletionResult syncActivityCompletion(Long courseId);

  ActivityCompletionResult getActivityCompletion(Long courseId);

  ActivityCompletionResult getActivityCompletion(
      Long courseId, List<Long> moduleIds, List<Long> userIds);
}
