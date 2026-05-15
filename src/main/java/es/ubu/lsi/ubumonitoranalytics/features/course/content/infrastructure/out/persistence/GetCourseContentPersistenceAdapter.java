package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.GetCourseContentPersistenceUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCourseContentPersistenceAdapter implements GetCourseContentPersistenceUseCase {

    private final CourseRepository courseRepository;
    private final CoursePersistenceMapper coursePersistenceMapper;

    @Override
    public CourseContent getCourseContent(Integer courseId) {
        return coursePersistenceMapper.toDomain(courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new));
    }
}
