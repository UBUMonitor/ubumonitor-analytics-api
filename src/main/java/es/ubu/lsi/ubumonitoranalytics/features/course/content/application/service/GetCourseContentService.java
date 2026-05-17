package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in.GetCourseContentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.GetCourseContentPersistenceUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class GetCourseContentService implements GetCourseContentUseCase {

    private final GetCourseContentPersistenceUseCase getCourseContentPersistenceUseCase;

    @Override
    @Transactional(readOnly = true)
    public CourseContent getCourseContent(Integer courseId) {
        return getCourseContentPersistenceUseCase.getCourseContent(courseId);
    }
}
