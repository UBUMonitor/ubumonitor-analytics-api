package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.in.GetUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.PersistenceFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserCoursesService implements GetUserCoursesUseCase {

  private final PersistenceFetchPort persistenceFetchPort;
  private final CurrentSessionContext currentSessionContext;

  @Override
  public UserEnrolledCourses getActualUserEnrollments() {
    SessionData sessionData = currentSessionContext.getSessionData();
    Integer userId = persistenceFetchPort.getUserIdByUserName(sessionData.getUsername());
    if (userId == null) {
      throw new EntityNotFoundException("User not found " + sessionData.getUsername());
    }
    return persistenceFetchPort.getEnrolledCourses(userId);
  }
}
