package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.in.SyncUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.out.EnrollmentCoursesFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.persistence.SitePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncUserCoursesService implements SyncUserCoursesUseCase {
    private final CurrentSessionContext currentSessionContext;
    private final EnrollmentCoursesFetchPort enrollmentCoursesFetchPort;
    private final SitePersistencePort sitePersistencePort;
    private final UserCoursePersistencePort userCoursePersistencePort;

    @Override
    @Transactional
    public UserCourses syncActualUserEnrollments() {
        SessionData sessionData = currentSessionContext.getSessionData();
        Integer userId = sitePersistencePort.getActualUserId(sessionData.getUserName());
        if (userId == null) {
            throw new EntityNotFoundException("User not found " + sessionData.getUserName());
        }
        UserCourses userCourses = enrollmentCoursesFetchPort.fetchEnrolledCourses(userId);

        userCoursePersistencePort.saveCourses(userCourses);

        return userCourses;
    }




}
