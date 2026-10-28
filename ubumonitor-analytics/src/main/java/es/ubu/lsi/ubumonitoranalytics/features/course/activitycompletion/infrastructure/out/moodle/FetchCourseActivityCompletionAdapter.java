package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.FetchActivityCompletionPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletion;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Fetches course sections and modules through the Moodle client. */
@Component
@RequiredArgsConstructor
public class FetchCourseActivityCompletionAdapter implements FetchActivityCompletionPort {

  private final Client client;
  private final FetchActivityCompletionAdapterMapper fetchCourseContentAdapterMapper;
  private final AsyncOperations asyncOperations;

  @Override
  public ActivityCompletionResult fetchCourseActivityCompletion(Long courseId, List<Long> userIds) {
    return ActivityCompletionResult.builder()
        .courseId(courseId)
        .activityCompletions(fetchInParallel(courseId, userIds))
        .build();
  }

  private List<ActivityCompletion> fetchInParallel(Long courseId, List<Long> userIds) {
    return userIds.stream()
        .map(
            userId ->
                asyncOperations.supplyAsync(() -> getUserActivitiesCompletion(courseId, userId)))
        .toList()
        .stream()
        .map(CompletableFuture::join)
        .flatMap(List::stream)
        .toList();
  }

  private List<ActivityCompletion> getUserActivitiesCompletion(Long courseId, Long userId) {
    var request = fetchCourseContentAdapterMapper.toRequest(courseId, userId);
    var response = client.coreCompletion().getActivitiesCompletionStatus(request);
    return fetchCourseContentAdapterMapper.toResponse(response.getStatuses(), userId);
  }
}
