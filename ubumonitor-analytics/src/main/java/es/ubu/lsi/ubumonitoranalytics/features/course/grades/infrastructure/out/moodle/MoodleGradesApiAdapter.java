package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.request.GetGradeItemsRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.request.GetGradesTableRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleGradesApiAdapter implements GradesApiPort {
  private final Client client;
  private final MoodleGradesAdapterRequestMapper moodleGradesAdapterRequestMapper;
  private final MoodleGradesAdapterResponseMapper moodleGradesAdapterResponseMapper;
  private final MoodleGradesAdapterTableResponseMapper moodleGradesAdapterTableResponseMapper;

  @Override
  public CourseGrade fetchCourseGradeItems(Long courseId, Long actualUserId) {

    GetGradeItemsRequestApi getGradeItemsRequestApi =
        moodleGradesAdapterRequestMapper.toRequest(courseId);

    GetGradeItemsResponseApi getGradeItemsResponseApi =
        client.gradeReportUser().getGradeItems(getGradeItemsRequestApi);

    CourseGrade courseGrade =
        moodleGradesAdapterResponseMapper.toCourseGradeItems(courseId, getGradeItemsResponseApi);

    courseGrade.setGradeItems(new ArrayList<>(courseGrade.getGradeItems()));
    fillCategoryParentIds(courseGrade, actualUserId);
    moodleGradesAdapterResponseMapper.assignPositions(courseGrade.getGradeItems());

    return courseGrade;
  }

  /**
   * Recently added parentcategory field for category items <a
   * href="https://moodle.atlassian.net/browse/MDL-64304">...</a>
   */
  private void fillCategoryParentIds(CourseGrade courseGrade, Long userId) {
    List<GradeItem> itemsNoParent =
        courseGrade.getGradeItems().stream()
            .filter(
                gradeItem ->
                    !Strings.CS.equals(gradeItem.getItemType(), "course")
                        && gradeItem.getParentId() == null)
            .toList();

    if (itemsNoParent.isEmpty()) {
      return;
    }

    GetGradesTableRequestApi getGradesTableRequestApi =
        moodleGradesAdapterRequestMapper.toGradesTableRequest(courseGrade.getCourseId(), userId);
    GetGradesTableResponseApi getGradesTableResponseApi =
        client.gradeReportUser().getGradesTable(getGradesTableRequestApi);

    moodleGradesAdapterTableResponseMapper.fillParent(
        getGradesTableResponseApi, itemsNoParent, courseGrade.getGradeItems());
  }
}
