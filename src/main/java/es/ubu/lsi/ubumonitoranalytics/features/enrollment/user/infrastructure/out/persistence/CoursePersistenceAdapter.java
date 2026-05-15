package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.CoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence.mapper.CoursePersistenceAdapterMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoursePersistenceAdapter implements CoursePersistencePort {
    private final CourseRepository courseRepository;
    private final CoursePersistenceAdapterMapper mapper;

    @Override
    public void saveAll(List<Course> courses) {
        courseRepository.saveAll(mapper.toEntity(courses));
    }
}
