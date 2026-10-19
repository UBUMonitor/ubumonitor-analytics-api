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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GradesSavePersistenceAdapter implements GradesSavePersistencePort {

  private final Jooq jooq;
  private final GradesSavePersistenceMapper mapper;

  @Override
  public void saveCourseGradeItemsPersistence(CourseGrade courseGrade) {
    Long courseId = courseGrade.getCourseId();
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    deactivateExisting(courseId, now);
    upsertGradeItems(courseGrade.getGradeItems(), courseId, now);
    upsertUserGrades(courseGrade.getUserGrades(), now);
  }

  private void deactivateExisting(Long courseId, LocalDateTime now) {
    jooq.dsl()
        .update(USERS_GRADES)
        .set(USERS_GRADES.ACTIVE, false)
        .set(USERS_GRADES.UPDATED_AT, now)
        .whereExists(
            jooq.dsl()
                .selectOne()
                .from(GRADES)
                .where(GRADES.ID.eq(USERS_GRADES.GRADE_ID))
                .and(GRADES.COURSE_ID.eq(courseId)))
        .and(USERS_GRADES.ACTIVE.isTrue())
        .execute();

    jooq.dsl()
        .update(GRADES)
        .set(GRADES.ACTIVE, false)
        .set(GRADES.UPDATED_AT, now)
        .where(GRADES.COURSE_ID.eq(courseId))
        .and(GRADES.ACTIVE.isTrue())
        .execute();
  }

  private void upsertGradeItems(List<GradeItem> gradeItems, Long courseId, LocalDateTime now) {
    if (gradeItems == null || gradeItems.isEmpty()) {
      return;
    }

    Set<Long> incomingIds = new HashSet<>();
    for (GradeItem gradeItem : gradeItems) {
      incomingIds.add(Objects.requireNonNull(gradeItem.getId(), "gradeItem id must not be null"));
    }

    Set<Long> persistedIds = new HashSet<>();
    List<GradeItem> pending = new ArrayList<>(gradeItems);
    while (!pending.isEmpty()) {
      List<GradeItem> nextPending = new ArrayList<>();
      List<GradesRecord> records = new ArrayList<>();

      for (GradeItem gradeItem : pending) {
        Long parentId = gradeItem.getParentId();
        if (parentId != null
            && incomingIds.contains(parentId)
            && !persistedIds.contains(parentId)) {
          nextPending.add(gradeItem);
          continue;
        }

        GradesRecord record = jooq.dsl().newRecord(GRADES);
        mapper.toRecord(gradeItem, record);
        record.setCourseId(courseId);
        record.setActive(true);
        record.setUpdatedAt(now);
        records.add(record);
        persistedIds.add(gradeItem.getId());
      }

      if (records.isEmpty()) {
        throw new IllegalStateException("Grade item hierarchy contains a cycle");
      }

      jooq.dsl().batchMerge(records).execute();
      pending = nextPending;
    }
  }

  private void upsertUserGrades(List<UserGrade> userGrades, LocalDateTime now) {
    if (userGrades == null || userGrades.isEmpty()) {
      return;
    }

    List<UsersGradesRecord> records = new ArrayList<>();
    for (UserGrade userGrade : userGrades) {
      UsersGradesRecord record = jooq.dsl().newRecord(USERS_GRADES);
      mapper.toRecord(userGrade, record);
      record.setActive(true);
      record.setUpdatedAt(now);
      records.add(record);
    }

    jooq.dsl().batchMerge(records).execute();
  }
}
