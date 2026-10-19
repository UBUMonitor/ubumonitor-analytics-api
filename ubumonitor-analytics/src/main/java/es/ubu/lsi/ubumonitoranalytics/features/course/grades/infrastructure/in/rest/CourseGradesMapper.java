package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradeItemsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;

/** Maps course grade information between REST DTOs and application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface CourseGradesMapper {

  CourseGradeItemsResponseDto toResponseDto(CourseGrade courseGrade);

  default CourseGradeItemsResponseDto toResponseDto(List<GradeItem> gradeItems) {
    CourseGrade courseGrade = new CourseGrade();
    courseGrade.setGradeItems(gradeItems);
    return toResponseDto(courseGrade);
  }
}
