package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import java.util.List;

public interface GradesUseCase {

  List<GradeItem> sync(Integer courseId);

  List<GradeItem> getGradeItems(Integer courseId);

  List<UserGrade> getUserGrades(Integer courseId);
}
