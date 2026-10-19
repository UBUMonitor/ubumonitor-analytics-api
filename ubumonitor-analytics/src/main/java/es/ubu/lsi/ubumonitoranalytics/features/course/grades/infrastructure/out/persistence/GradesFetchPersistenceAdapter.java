package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Grades.GRADES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGrades.USERS_GRADES;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesFetchPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradesFetchPersistenceAdapter implements GradesFetchPersistencePort {

  private final Jooq jooq;
  private final GradesFetchPersistenceMapper mapper;

  @Override
  public List<GradeItem> fetchCourseGradeItemsPersistence(Integer courseId) {
    return jooq.dsl()
        .selectFrom(GRADES)
        .where(GRADES.COURSE_ID.eq(courseId).and(GRADES.ACTIVE.isTrue()))
        .fetch()
        .map(mapper::toGradeItem);
  }

  @Override
  public List<UserGrade> fetchCourseUserGradesPersistence(Integer courseId) {
    return jooq.dsl()
        .select(USERS_GRADES.fields())
        .from(USERS_GRADES)
        .join(GRADES)
        .on(GRADES.ID.eq(USERS_GRADES.GRADE_ID))
        .where(GRADES.COURSE_ID.eq(courseId).and(GRADES.ACTIVE.isTrue()))
        .and(USERS_GRADES.ACTIVE.isTrue())
        .fetchInto(USERS_GRADES) // Result<UsersGradesRecord>
        .map(mapper::toUserGrade);
  }
}
