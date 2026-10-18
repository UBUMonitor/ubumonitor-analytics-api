package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesFetchPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradesFetchPersistenceAdapter implements GradesFetchPersistencePort {
    private final Jooq jooq;

    @Override
    public CourseGrade fetchCourseGradeItemsPersistence(Integer courseId) {
        return jooq.dsl().select(GRADE_ITEMS);
    }
}
