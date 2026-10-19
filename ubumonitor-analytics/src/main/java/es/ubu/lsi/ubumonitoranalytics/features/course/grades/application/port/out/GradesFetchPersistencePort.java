package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import java.util.List;

public interface GradesFetchPersistencePort {

  List<GradeItem> fetchCourseGradeItemsPersistence(Integer courseId);

  List<UserGrade> fetchCourseUserGradesPersistence(Integer courseId);
}
