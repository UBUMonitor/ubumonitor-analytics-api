package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import java.util.List;

public interface GradesFetchPersistencePort {

  List<GradeItem> fetchCourseGradeItemsPersistence(Long courseId);

  List<UserGrade> fetchCourseUserGradesPersistence(
      Long courseId, List<Long> gradeItemsIds, List<Long> userIds);
}
