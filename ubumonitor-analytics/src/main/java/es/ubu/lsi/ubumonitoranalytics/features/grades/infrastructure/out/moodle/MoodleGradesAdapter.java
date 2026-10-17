package es.ubu.lsi.ubumonitoranalytics.features.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.request.GetGradeItemsRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.request.GetGradesTableRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model.CourseGradeItem;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleGradesAdapter {
  private final Client client;
  private final MoodleGradesAdapterRequestMapper moodleGradesAdapterRequestMapper;
  private final MoodleGradesAdapterResponseMapper moodleGradesAdapterResponseMapper;
  private final AsyncOperations asyncOperations;

  public CourseGradeItem fetchCourseGradeItems(Integer courseId, Integer actualUserId) {

    GetGradeItemsRequestApi getGradeItemsRequestApi =
        moodleGradesAdapterRequestMapper.toRequest(courseId);
    GetGradesTableRequestApi getGradesTableRequestApi =
        moodleGradesAdapterRequestMapper.toGradesTableRequest(courseId);

    CompletableFuture<GetGradeItemsResponseApi> gradeItemsCall =
        asyncOperations.supplyAsync(
            () -> client.gradeReportUser().getGradeItems(getGradeItemsRequestApi));

    CompletableFuture<GetGradesTableResponseApi> gradesTableCall =
        asyncOperations.supplyAsync(
            () -> client.gradeReportUser().getGradesTable(getGradesTableRequestApi));

    return moodleGradesAdapterResponseMapper.toCourseGradeItems(
        courseId, actualUserId, gradeItemsCall.join(), gradesTableCall.join());
  }
}
