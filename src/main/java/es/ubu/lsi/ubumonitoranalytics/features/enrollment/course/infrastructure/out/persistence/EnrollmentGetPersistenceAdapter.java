package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.EnrollmentGetPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.EnrollmentCourseEnrollmentsMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnrollmentGetPersistenceAdapter implements EnrollmentGetPersistencePort {

    private final UserCourseRepository  userCourseRepository;
    private final EnrollmentCourseEnrollmentsMapper enrollmentCourseEnrollmentsMapper;

    @Override
    public CourseEnrollment getCourseEnrollment(Integer courseId) {
        List<UserCourseEntity> userCourseEntities =  userCourseRepository.findByCourseId(courseId);

        return enrollmentCourseEnrollmentsMapper.toDomain(courseId, userCourseEntities);
    }
}

