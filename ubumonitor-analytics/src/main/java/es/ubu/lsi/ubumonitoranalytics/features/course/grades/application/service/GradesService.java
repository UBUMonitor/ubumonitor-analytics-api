package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.in.GradesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.CurrentUserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesFetchPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesSavePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradesService implements GradesUseCase {

  private final CurrentUserPersistencePort currentUserPersistencePort;
  private final GradesFetchPersistencePort gradesFetchPersistencePort;
  private final GradesSavePersistencePort gradesSavePersistencePort;
  private final CurrentSessionContext currentSessionContext;
  private final GradesApiPort gradesApiPort;

  @Override
  @Transactional
  public List<GradeItem> sync(Long courseId) {
    Long userId =
        currentUserPersistencePort.getUserId(currentSessionContext.getSessionData().getUsername());
    CourseGrade courseGrade = gradesApiPort.fetchCourseGradeItems(courseId, userId);
    gradesSavePersistencePort.saveCourseGradeItemsPersistence(courseGrade);
    return courseGrade.getGradeItems();
  }

  @Override
  public List<GradeItem> getGradeItems(Long courseId) {
    return gradesFetchPersistencePort.fetchCourseGradeItemsPersistence(courseId);
  }

  @Override
  public List<UserGrade> getUserGrades(Long courseId) {
    return gradesFetchPersistencePort.fetchCourseUserGradesPersistence(courseId);
  }
}
