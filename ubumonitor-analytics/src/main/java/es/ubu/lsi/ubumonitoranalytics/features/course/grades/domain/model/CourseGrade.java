package es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model;

import java.util.List;
import lombok.Data;

@Data
public class CourseGrade {
  private Integer courseId;
  private List<GradeItem> gradeItems;
  private List<UserGrade> userGrades;
}
