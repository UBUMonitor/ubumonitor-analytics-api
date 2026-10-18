package es.ubu.lsi.ubumonitoranalytics.features.grades.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.grades.application.port.in.GradesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.grades.application.port.out.CurrentUserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.grades.application.port.out.GradesPort;
import es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradesService implements GradesUseCase {

  private final CurrentUserPersistencePort currentUserPersistencePort;
  private final CurrentSessionContext currentSessionContext;
  private final GradesPort gradesPort;

  @Override
  public CourseGrade sync(Integer courseId) {
    Integer userId =
        currentUserPersistencePort.getUserId(currentSessionContext.getSessionData().getUsername());
    return gradesPort.fetchCourseGradeItems(courseId, userId);
  }

  @Override
  public CourseGrade get(Integer courseId) {
    return null;
  }
}
