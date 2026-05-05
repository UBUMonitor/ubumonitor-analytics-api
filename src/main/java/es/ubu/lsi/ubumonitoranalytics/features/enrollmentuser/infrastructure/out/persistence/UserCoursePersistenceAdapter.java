package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class UserCoursePersistenceAdapter implements UserCoursePersistencePort {
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final UserRepository userRepository;
    private final UserCoursePersistenceAdapterMapper userCoursePersistenceAdapterMapper;


    @Override
    public void saveCourses(UserCourses userCourses) {

        // 1. Obtener o crear usuario
        UserEntity user = userRepository.findById(userCourses.getUserId())
            .orElseGet(() -> {
                UserEntity newUser = new UserEntity();
                newUser.setId(userCourses.getUserId());
                return userRepository.save(newUser);
            });

        // 2. Limpiar relaciones actuales (orphanRemoval = true se encarga de deletes)
        user.getUserCourses().clear();

        // 3. MAPEAR Y PERSISTIR COURSES PRIMERO (CRÍTICO)
        List<CourseEntity> courses =
            userCoursePersistenceAdapterMapper.toCourseEntity(userCourses.getCourses());

        courses = courseRepository.saveAll(courses);

        // 4. Indexar cursos por ID para reutilizar entidades gestionadas
        Map<Integer, CourseEntity> courseMap = courses.stream()
            .collect(Collectors.toMap(CourseEntity::getId, c -> c));

        // 5. Crear relaciones user-course
        List<UserCourseEntity> userCourseEntities =
            userCoursePersistenceAdapterMapper.toUserCourseEntities(userCourses, true);

        userCourseEntities.forEach(uc -> {

            uc.setUser(user);
            if (uc.getCourse() != null && uc.getCourse().getId() != null) {
                CourseEntity managedCourse = courseMap.get(uc.getCourse().getId());

                if (managedCourse == null) {
                    throw new IllegalStateException(
                        "Course not found in current tenant: " + uc.getCourse().getId()
                    );
                }

                uc.setCourse(managedCourse);
            }
        });

        user.getUserCourses().addAll(userCourseEntities);

        userRepository.save(user);
    }

    @Override
    public List<Course> getUserCourses(Integer userId) {
        return userCoursePersistenceAdapterMapper.toDomain(courseRepository.findAll());
    }


}
