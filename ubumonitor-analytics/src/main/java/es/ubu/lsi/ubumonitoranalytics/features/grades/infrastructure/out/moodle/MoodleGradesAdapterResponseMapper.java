package es.ubu.lsi.ubumonitoranalytics.features.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GradeItemApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.UserGradeApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model.CourseGradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterResponseMapper {

  @Mapping(
      target = "userGrades",
      expression = "java(toUserGrades(getGradeItemsResponseApi, getGradesTableResponseApi))")
  @Mapping(target = "courseId", source = "courseId")
  CourseGradeItem toCourseGradeItems(
      Integer courseId,
      Integer actualUserId,
      GetGradeItemsResponseApi getGradeItemsResponseApi,
      GetGradesTableResponseApi getGradesTableResponseApi);

  default List<UserGrade> toUserGrades(
      GetGradeItemsResponseApi getGradeItemsResponseApi,
      GetGradesTableResponseApi getGradesTableResponseApi) {
    List<UserGradeApi> userGrades = getGradeItemsResponseApi.getUsergrades();
    if (userGrades == null) {
      return null;
    }

    List<UserGrade> result = new ArrayList<>();

    for (UserGradeApi userGradeApi : userGrades) {
      for (GradeItemApi gradeItemApi : userGradeApi.getGradeitems()) {
        result.add(toUserGrade(userGradeApi, gradeItemApi));
      }
    }
    return result;
  }

  @Mapping(target = "gradeItemId", source = "gradeItemApi.id")
  @Mapping(target = "grade", source = "gradeItemApi.graderaw")
  @Mapping(target = "userId", source = "userGradeApi.userid")
  UserGrade toUserGrade(UserGradeApi userGradeApi, GradeItemApi gradeItemApi);
}
