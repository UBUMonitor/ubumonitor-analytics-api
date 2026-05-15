package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserCourseMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseId;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserCoursePersistenceAdapter implements UserCoursePersistencePort {

    private final UserCourseRepository userCourseRepository;
    private final UserCourseMapper userCourseMapper;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Override
    public void syncUserCourses(
        List<Integer> userIds,
        List<Enrollment> enrollments,
        Integer courseId
    ) {

        // 1. EXISTENTES (solo del curso)
        List<UserCourseEntity> existing = userCourseRepository
            .findByUserIdInAndCourseId(userIds, courseId);

        // 2. MAP por PK compuesta
        Map<UserCourseId, UserCourseEntity> existingMap = existing.stream()
            .collect(Collectors.toMap(
                UserCourseEntity::getId,
                Function.identity()
            ));

        // 3. soft delete base
        existing.forEach(e -> e.setActive(false));

        List<UserCourseEntity> result = new ArrayList<>();

        // 4. procesar nuevos estados
        for (Enrollment enrollment : enrollments) {

            User user = enrollment.getUser();

            // ======================
            // relación principal
            // ======================
            result.add(
                process(existingMap, enrollment)
            );

            // ======================
            // relaciones derivadas
            // ======================
            for (Course course : user.getCourses()) {

                if (course.getId().equals(courseId)) {
                    continue;
                }

                Enrollment derived = new Enrollment();
                derived.setUser(user);
                derived.setCourse(course);

                result.add(
                    process(existingMap, derived)
                );
            }
        }

        userCourseRepository.saveAll(result);
    }

    private UserCourseEntity process(
        Map<UserCourseId, UserCourseEntity> existingMap,
        Enrollment enrollment
    ) {

        UserCourseId id = new UserCourseId(
            enrollment.getUser().getId(),
            enrollment.getCourse().getId()
        );

        UserCourseEntity entity = existingMap.get(id);

        if (entity != null) {
            // ✔ EXISTE → reactivar
            entity.setActive(true);
            return entity;
        }

        // ➕ NO EXISTE → crear
        UserCourseEntity newEntity = userCourseMapper.toEntity(enrollment, true);

        newEntity.setUser(
            userRepository.getReferenceById(enrollment.getUser().getId())
        );

        newEntity.setCourse(
            courseRepository.getReferenceById(enrollment.getCourse().getId())
        );

        return newEntity;
    }
}
