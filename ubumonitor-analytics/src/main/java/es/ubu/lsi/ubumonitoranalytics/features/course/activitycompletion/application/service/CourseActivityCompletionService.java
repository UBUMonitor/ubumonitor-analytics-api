package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.in.CourseActivityCompletionUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.CourseActivityCompletionPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.CourseUsersPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.FetchActivityCompletionPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.GetCourseActivityCompletionPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Fetches course content from Moodle and stores it for the current tenant. */
@Service
@RequiredArgsConstructor
public class CourseActivityCompletionService implements CourseActivityCompletionUseCase {

  private final CourseActivityCompletionPersistencePort persistencePort;
  private final FetchActivityCompletionPort fetchActivityCompletionPort;
  private final GetCourseActivityCompletionPersistencePort
      getCourseActivityCompletionPersistencePort;
  private final CourseUsersPort courseUsersPort;

  @Override
  public ActivityCompletionResult syncActivityCompletion(Long courseId) {
    List<Long> usersIds = courseUsersPort.getCourseUsersIds(courseId);
    ActivityCompletionResult activityCompletionResult =
        fetchActivityCompletionPort.fetchCourseActivityCompletion(courseId, usersIds);
    persistencePort.save(activityCompletionResult);
    return activityCompletionResult;
  }

  @Override
  public ActivityCompletionResult getActivityCompletion(Long courseId) {
    return getCourseActivityCompletionPersistencePort.getCourseActivitiesCompletions(courseId);
  }

  @Override
  public ActivityCompletionResult getActivityCompletion(
      Long courseId, List<Long> moduleIds, List<Long> userIds) {
    return getCourseActivityCompletionPersistencePort.getCourseActivitiesCompletions(
        courseId, moduleIds, userIds);
  }
}
