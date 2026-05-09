package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.EnrollmentGetPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.UserCourseMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnrollmentGetPersistenceAdapter implements EnrollmentGetPersistencePort {

    private final UserCourseRepository  userCourseRepository;
    private final UserCourseMapper userCourseMapper;

    @Override
    public CourseEnrollment getCourseEnrollment(Integer courseId) {
        List<UserCourseEntity> userCourseEntities =  userCourseRepository.findByCourseId(courseId);

        return userCourseMapper.toDomain(courseId, userCourseEntities);
    }
}
