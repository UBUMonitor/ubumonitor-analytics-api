package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.in.GradesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.CurrentUserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesFetchPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradesService implements GradesUseCase {

  private final CurrentUserPersistencePort currentUserPersistencePort;
  private final GradesFetchPersistencePort gradesFetchPersistencePort;
  private final CurrentSessionContext currentSessionContext;
  private final GradesApiPort gradesApiPort;

  @Override
  public CourseGrade sync(Integer courseId) {
    Integer userId =
        currentUserPersistencePort.getUserId(currentSessionContext.getSessionData().getUsername());
    CourseGrade courseGrade = gradesApiPort.fetchCourseGradeItems(courseId, userId);

    return courseGrade;
  }

  @Override
  public CourseGrade get(Integer courseId) {
    return gradesFetchPersistencePort.fetchCourseGradeItemsPersistence(courseId);
  }
}
