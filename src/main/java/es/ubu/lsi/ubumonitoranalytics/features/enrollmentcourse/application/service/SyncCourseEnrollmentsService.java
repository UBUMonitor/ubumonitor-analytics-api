package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.out.EnrollmentFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.out.EnrollmentSavePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SyncCourseEnrollmentsService implements SyncCourseEnrollmentsUseCase {

    private final EnrollmentFetchPort enrollmentFetchPort;
    private final EnrollmentSavePersistencePort enrollmentSavePersistencePort;

    @Override
    @Transactional
    public CourseEnrollment syncCourseEnrollments(Integer courseId) {
        CourseEnrollment courseEnrollment = enrollmentFetchPort.fetchCourseEnrolledUsers(courseId);

        enrollmentSavePersistencePort.saveEnrollments(courseEnrollment);

        return courseEnrollment;
    }



}
