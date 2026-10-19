package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import java.util.List;

public interface GradesUseCase {

  List<GradeItem> sync(Long courseId);

  List<GradeItem> getGradeItems(Long courseId);

  List<UserGrade> getUserGrades(Long courseId);
}
