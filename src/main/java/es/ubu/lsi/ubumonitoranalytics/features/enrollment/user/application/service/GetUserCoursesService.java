package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.in.GetUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.persistence.SitePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GetUserCoursesService implements GetUserCoursesUseCase {

    private final EnrollmentPersistencePort enrollmentPersistencePort;
    private final SitePersistencePort sitePersistencePort;
    private final CurrentSessionContext currentSessionContext;

    @Override
    public UserEnrolledCourses getActualUserEnrollments() {
        SessionData sessionData = currentSessionContext.getSessionData();
        Integer userId = sitePersistencePort.getActualUserId(sessionData.getUserName());
        if (userId == null) {
            throw new EntityNotFoundException("User not found " + sessionData.getUserName());
        }
        return enrollmentPersistencePort.fetchUserEnrolledCourses(userId);
    }
}

