package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.EnrollmentFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.EnrollmentSavePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncCourseEnrollmentsService implements SyncCourseEnrollmentsUseCase {

    private final EnrollmentFetchPort enrollmentFetchPort;
    private final EnrollmentSavePersistencePort enrollmentSavePersistencePort;
    private final CurrentSessionContext currentSessionContext;

    @Override
    @Transactional
    public CourseEnrollment syncCourseEnrollments(Integer courseId) {
        CourseEnrollment courseEnrollment = enrollmentFetchPort.fetchCourseEnrolledUsers(courseId, currentSessionContext.getSessionData().getMoodleToken());

        enrollmentSavePersistencePort.saveEnrollments(courseEnrollment);

        return courseEnrollment;
    }



}
