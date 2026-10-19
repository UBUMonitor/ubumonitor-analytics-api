package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GradeItemApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.UserGradeApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterResponseMapper {

  @Mapping(target = "userGrades", source = "getGradeItemsResponseApi")
  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "gradeItems", source = "getGradeItemsResponseApi")
  CourseGrade toCourseGradeItems(Long courseId, GetGradeItemsResponseApi getGradeItemsResponseApi);

  default List<GradeItem> toGradeItems(GetGradeItemsResponseApi getGradeItemsResponseApi) {
    if (getGradeItemsResponseApi == null) {
      return List.of();
    }
    List<UserGradeApi> userGrades = getGradeItemsResponseApi.getUsergrades();
    if (userGrades == null) {
      return List.of();
    }

    for (UserGradeApi userGrade : userGrades) {
      if (userGrade == null || userGrade.getGradeitems() == null) {
        continue;
      }
      return userGrade.getGradeitems().stream()
          .map(gradeItem -> toGradeItem(gradeItem, userGrade.getGradeitems()))
          .toList();
    }
    return List.of();
  }

  @Mapping(target = "itemInstance", source = "iteminstance")
  @Mapping(target = "itemType", source = "itemtype")
  @Mapping(target = "name", source = "itemname")
  @Mapping(target = "gradeMax", source = "grademax")
  @Mapping(target = "gradeMin", source = "grademin")
  @Mapping(target = "id", source = "id")
  @Mapping(target = "parentId", source = "gradeItemApi", qualifiedByName = "parentId")
  GradeItem toGradeItem(GradeItemApi gradeItemApi, @Context List<GradeItemApi> allGradeItems);

  @Named("parentId")
  default Long parentId(GradeItemApi gradeItemApi, @Context List<GradeItemApi> allGradeItems) {
    if ("category".equals(gradeItemApi.getItemtype())) {
      return allGradeItems.stream()
          .filter(
              anotherGradeItem ->
                  Objects.equals(
                      anotherGradeItem.getCategoryid(), gradeItemApi.getParentcategoryid()))
          .findAny()
          .map(GradeItemApi::getId)
          .map(Long::valueOf)
          .orElse(null);
    }
    return allGradeItems.stream()
        .filter(
            anotherGradeItem ->
                Objects.equals(anotherGradeItem.getIteminstance(), gradeItemApi.getCategoryid()))
        .findAny()
        .map(GradeItemApi::getId)
        .map(Long::valueOf)
        .orElse(null);
  }

  default List<UserGrade> toUserGrades(GetGradeItemsResponseApi getGradeItemsResponseApi) {
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

  @Mapping(
      target = "gradePercentageRaw",
      source = "gradeItemApi",
      qualifiedByName = "gradePercentageRaw")
  @Mapping(target = "gradeItemId", source = "gradeItemApi.id")
  @Mapping(target = "gradeRaw", source = "gradeItemApi.graderaw")
  @Mapping(target = "userId", source = "userGradeApi.userid")
  UserGrade toUserGrade(UserGradeApi userGradeApi, GradeItemApi gradeItemApi);

  @Named("gradePercentageRaw")
  default BigDecimal gradePercentageRaw(GradeItemApi gradeItemApi) {
    Double gradeRaw = gradeItemApi.getGraderaw();
    Double min = gradeItemApi.getGrademin();
    Double max = gradeItemApi.getGrademax();
    if (gradeRaw == null || min == null || max == null) {
      return null;
    }

    return BigDecimal.valueOf((gradeRaw - min) / (max - min) * 100)
        .setScale(2, RoundingMode.HALF_UP);
  }
}
