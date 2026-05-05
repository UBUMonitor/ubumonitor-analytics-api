package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.out.EnrollmentGetPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCourseEnrollmentsService implements GetCourseEnrollmentsUseCase {

    private final EnrollmentGetPersistencePort enrollmentGetPersistencePort;

    @Override
    @Transactional(readOnly = true)
    public CourseEnrollment getCourseEnrollment(Integer courseId) {
        return enrollmentGetPersistencePort.getCourseEnrollment(courseId);
    }
}
