package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import java.util.List;

/** Fetches course content from Moodle. */
public interface FetchActivityCompletionPort {

  ActivityCompletionResult fetchCourseActivityCompletion(Long courseId, List<Long> userIds);
}
