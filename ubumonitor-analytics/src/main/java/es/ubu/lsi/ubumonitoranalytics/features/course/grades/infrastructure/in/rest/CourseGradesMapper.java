package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradeItemsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradesValuesResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradesValuesResponseRowDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps course grade information between REST DTOs and application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface CourseGradesMapper {

  CourseGradeItemsResponseDto toCourseGradeItemsResponseDto(CourseGrade courseGrade);

  @Mapping(target = "gradeItems", source = "gradeItems")
  CourseGradeItemsResponseDto toCourseGradeItemsResponseDto(
      List<GradeItem> gradeItems, Long courseId);

  @Mapping(target = "grades", source = "userGrades")
  CourseGradesValuesResponseDto toCourseGradesValuesResponseDto(
      List<UserGrade> userGrades, Long courseId);

  @Mapping(target = "percentage", source = "gradePercentageRaw")
  @Mapping(target = "id", source = "gradeItemId")
  @Mapping(target = "grade", source = "gradeRaw")
  @Mapping(target = "userId", source = "userId")
  CourseGradesValuesResponseRowDto toCourseGradesValuesResponseRowDto(UserGrade userGrade);
}
