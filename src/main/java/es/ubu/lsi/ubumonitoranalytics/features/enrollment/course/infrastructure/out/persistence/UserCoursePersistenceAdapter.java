package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserCourseMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        userCourseRepository.deactivateByUserIds(userIds);

        List<UserCourseEntity> entities = new ArrayList<>();

        for (Enrollment enrollment : enrollments) {

            // relación principal (curso actual)
            UserCourseEntity userCourseEntity = userCourseMapper.toEntity(enrollment, true);
            userCourseEntity.setUser(userRepository.getReferenceById(enrollment.getUser().getId()));
            userCourseEntity.setCourse(courseRepository.getReferenceById(enrollment.getCourse().getId()));
            entities.add(userCourseEntity);

            User user = enrollment.getUser();

            // relaciones derivadas
            for (Course course : user.getCourses()) {

                if (course.getId().equals(courseId)) {
                    continue;
                }

                Enrollment relation = new Enrollment();
                relation.setUser(user);
                relation.setCourse(course);
                userCourseEntity = userCourseMapper.toEntity(enrollment, true);
                userCourseEntity.setUser(userRepository.getReferenceById(enrollment.getUser().getId()));
                userCourseEntity.setCourse(courseRepository.getReferenceById(enrollment.getCourse().getId()));
                entities.add(
                    userCourseEntity
                );
            }
        }

        userCourseRepository.saveAll(entities);
    }
}
