package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.in.SyncUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentCoursesApiFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.PersistenceFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.SyncEnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
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
    private final EnrollmentCoursesApiFetchPort enrollmentCoursesApiFetchPort;
    private final SyncEnrollmentPersistencePort enrollmentPort;
    private final PersistenceFetchPort persistenceFetchPort;

    @Override
    @Transactional
    public UserEnrolledCourses syncActualUserEnrollments() {
        SessionData sessionData = currentSessionContext.getSessionData();
        Integer userId = persistenceFetchPort.getUserIdByUserName(sessionData.getUsername());
        if (userId == null) {
            throw new EntityNotFoundException("User not found " + sessionData.getUsername());
        }
        UserEnrolledCourses userEnrolledCourses = enrollmentCoursesApiFetchPort.fetchEnrolledCourses(userId);


        enrollmentPort.sync(userEnrolledCourses); // 3. Sync matrículas

        return userEnrolledCourses;
    }




}

