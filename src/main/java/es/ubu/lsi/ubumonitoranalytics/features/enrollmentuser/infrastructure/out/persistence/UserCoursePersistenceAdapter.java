package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseId;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        UserEntity user = userRepository.findById(userCourses.getUserId())
            .orElseGet(()-> {
                UserEntity userEntity = new UserEntity();
                userEntity.setId(userCourses.getUserId());
                return userEntity;
            });

        if (user.getUserCourses() == null) {
            user.setUserCourses(new  ArrayList<>());
        }

        Map<Integer, UserCourseEntity> existing = user.getUserCourses().stream()
            .collect(Collectors.toMap(
                uc -> uc.getCourse().getId(),
                uc -> uc
            ));

        Set<Integer> incomingIds = userCourses.getCourses().stream()
            .map(Course::getId)
            .collect(Collectors.toSet());

        // 1. DESACTIVAR LOS QUE YA NO VIENEN
        user.getUserCourses().forEach(uc -> {
            if (!incomingIds.contains(uc.getCourse().getId())) {
                uc.setActive(false);
            }
        });

        // 2. ACTIVAR O CREAR LOS NUEVOS
        List<CourseEntity> courses =
            userCoursePersistenceAdapterMapper.toCourseEntity(userCourses.getCourses());

        courses = courseRepository.saveAll(courses);

        Map<Integer, CourseEntity> courseMap = courses.stream()
            .collect(Collectors.toMap(CourseEntity::getId, c -> c));

        for (Course course : userCourses.getCourses()) {

            UserCourseEntity uc = existing.get(course.getId());

            if (uc == null) {
                uc = new UserCourseEntity();
                uc.setId(new UserCourseId(user.getId(), course.getId()));
                uc.setUser(user);
                uc.setCourse(courseMap.get(course.getId()));
                uc.setActive(true);

                user.getUserCourses().add(uc);

            } else {
                uc.setActive(true); // reactivar si ya existía
            }
        }

        userRepository.save(user);
    }
    @Override
    public List<Course> getUserCourses(Integer userId) {
        return userCoursePersistenceAdapterMapper.toDomain(courseRepository.findAll());
    }


}
