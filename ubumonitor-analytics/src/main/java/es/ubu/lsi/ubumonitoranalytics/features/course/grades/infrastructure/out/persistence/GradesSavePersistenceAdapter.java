package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Grades.GRADES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGrades.USERS_GRADES;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesSavePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GradesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGradesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
public class GradesSavePersistenceAdapter implements GradesSavePersistencePort {

  private final Jooq jooq;
  private final GradesSavePersistenceMapper mapper;

  @Override
  public void saveCourseGradeItemsPersistence(CourseGrade courseGrade) {
    Long courseId = courseGrade.getCourseId();
    Long now = Instant.now().getEpochSecond();

    deactivateExisting(courseId, now);
    upsertGradeItems(courseGrade.getGradeItems(), courseId, now);
    upsertUserGrades(courseGrade.getUserGrades(), now);
  }

  // ---------- Deactivate ----------

  private void deactivateExisting(Long courseId, Long now) {
    deactivateUserGrades(courseId, now);
    deactivateGrades(courseId, now);
  }

  private void deactivateUserGrades(Long courseId, Long now) {
    jooq.dsl()
        .update(USERS_GRADES)
        .set(USERS_GRADES.ACTIVE, false)
        .set(USERS_GRADES.UPDATED_AT, now)
        .where(USERS_GRADES.ACTIVE.isTrue())
        .and(USERS_GRADES.GRADE_ID.in(courseGradeIds(courseId)))
        .execute();
  }

  private void deactivateGrades(Long courseId, Long now) {
    jooq.dsl()
        .update(GRADES)
        .set(GRADES.ACTIVE, false)
        .set(GRADES.UPDATED_AT, now)
        .where(GRADES.COURSE_ID.eq(courseId))
        .and(GRADES.ACTIVE.isTrue())
        .execute();
  }

  private Select<Record1<Long>> courseGradeIds(Long courseId) {
    return jooq.dsl().select(GRADES.ID).from(GRADES).where(GRADES.COURSE_ID.eq(courseId));
  }

  // ---------- Upsert ----------

  private void upsertGradeItems(List<GradeItem> gradeItems, Long courseId, Long now) {
    if (CollectionUtils.isEmpty(gradeItems)) {
      return;
    }

    List<GradesRecord> records =
        gradeItems.stream().map(item -> mapper.toRecord(item, courseId, now)).toList();
    jooq.dsl().batchMerge(records).execute();
  }

  private void upsertUserGrades(List<UserGrade> userGrades, Long now) {
    if (CollectionUtils.isEmpty(userGrades)) {
      return;
    }

    List<UsersGradesRecord> records =
        userGrades.stream().map(userGrade -> mapper.toRecord(userGrade, now)).toList();
    jooq.dsl().batchMerge(records).execute();
  }
}
