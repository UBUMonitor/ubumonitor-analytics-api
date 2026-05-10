package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.CoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.CourseMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EnrollmentCoursePersistenceAdapter implements CoursePersistencePort {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Override
    public void syncCourses(Collection<Course> courses) {

        if (courses.isEmpty()) {
            return;
        }

        Map<Integer, Course> coursesById = courses.stream()
            .collect(Collectors.toMap(Course::getId, c -> c));

        List<CourseEntity> existing =
            courseRepository.findAllById(coursesById.keySet());

        Set<Integer> existingIds = existing.stream()
            .map(CourseEntity::getId)
            .collect(Collectors.toSet());

        // update existing
        for (CourseEntity entity : existing) {
            Course course = coursesById.get(entity.getId());

            courseMapper.updateCourseEntity(course, entity);
        }

        // create missing
        List<CourseEntity> toCreate = courses.stream()
            .filter(c -> !existingIds.contains(c.getId()))
            .map(courseMapper::createCourseEntity)
            .toList();

        if (!toCreate.isEmpty()) {
            courseRepository.saveAll(toCreate);
        }
    }
}
