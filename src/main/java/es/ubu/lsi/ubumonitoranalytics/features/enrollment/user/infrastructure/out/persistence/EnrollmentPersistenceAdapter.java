package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence.mapper.EnrollmentPersistenceAdapterMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseId;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EnrollmentPersistenceAdapter implements EnrollmentPersistencePort {

    private final UserCourseRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentPersistenceAdapterMapper mapper;

    @Override
    public void sync(Integer userId, List<Enrollment> enrolledCours) {


        // 1. Soft-delete los que ya no vienen
        enrollmentRepository.deactivateByUserIds(List.of(userId));

        // 2. Upsert actuales
        for (Enrollment enrolled : enrolledCours) {
            Integer courseId = enrolled.getCourse().getId();
            UserCourseId compositeId = new UserCourseId(userId, courseId);

            enrollmentRepository.findById(compositeId)
                .ifPresentOrElse(
                    existing -> {
                        mapper.updateEntity(enrolled, existing);
                        existing.setActive(true); // reactivar si estaba desactivado
                    },
                    () -> saveEnrollment(userId, enrolled, compositeId, courseId)

                );
        }
    }

    private void saveEnrollment(Integer userId, Enrollment enrolled, UserCourseId compositeId, Integer courseId) {
        UserCourseEntity entity = mapper.toNewEntity(enrolled);
        // El adapter monta la infraestructura JPA que MapStruct no toca
        entity.setId(compositeId);
        entity.setUser(userRepository.getReferenceById(userId));
        entity.setCourse(courseRepository.getReferenceById(courseId));
        entity.setActive(true);
        enrollmentRepository.save(entity);
    }

    @Override
    public UserEnrolledCourses fetchUserEnrolledCourses(Integer userId) {
        return mapper.toDomain(userId, enrollmentRepository.findByUserId(userId).orElseThrow(EntityNotFoundException::new));
    }
}
