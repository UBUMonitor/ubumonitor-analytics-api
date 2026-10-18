package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.request.GetGradeItemsRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.request.GetGradesTableRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
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
  public CourseGrade fetchCourseGradeItems(Integer courseId, Integer actualUserId) {

    GetGradeItemsRequestApi getGradeItemsRequestApi =
        moodleGradesAdapterRequestMapper.toRequest(courseId);

    GetGradeItemsResponseApi getGradeItemsResponseApi =
        client.gradeReportUser().getGradeItems(getGradeItemsRequestApi);

    CourseGrade courseGrade =
        moodleGradesAdapterResponseMapper.toCourseGradeItems(
            courseId, getGradeItemsResponseApi, actualUserId);

    fillCategoryParentIds(courseGrade, actualUserId);

    return courseGrade;
  }

  /**
   * Recently added parentcategory field for category items <a
   * href="https://moodle.atlassian.net/browse/MDL-64304">...</a>
   */
  private void fillCategoryParentIds(CourseGrade courseGrade, Integer userId) {
    List<GradeItem> categoriesNoParent =
        courseGrade.getGradeItems().stream()
            .filter(
                gradeItem ->
                    Strings.CS.equalsAny(gradeItem.getItemType(), "category", "course")
                        && gradeItem.getParentId() == null)
            .toList();

    if (categoriesNoParent.size() <= 1) {
      return;
    }

    GetGradesTableRequestApi getGradesTableRequestApi =
        moodleGradesAdapterRequestMapper.toGradesTableRequest(courseGrade.getCourseId(), userId);
    GetGradesTableResponseApi getGradesTableResponseApi =
        client.gradeReportUser().getGradesTable(getGradesTableRequestApi);

    moodleGradesAdapterTableResponseMapper.fillParent(
        getGradesTableResponseApi, categoriesNoParent);
  }
}
