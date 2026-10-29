package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Grades.GRADES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGrades.USERS_GRADES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.math.BigDecimal;
import java.nio.file.Path;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;

class GradesSavePersistenceAdapterTest {

  @TempDir Path tempDir;

  @Test
  void synchronizesGradeItemsAndUserGradesAndDeactivatesMissingRows() throws Exception {
    String jdbcUrl =
        "jdbc:sqlite:" + tempDir.resolve("grades-sync.db").toString().replace("\\", "/");
    try (var connection = DatabaseUtil.createDataSource(jdbcUrl, "test-key").getConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);
      createTables(dsl);
      insertGrade(dsl, 11L, 7L);
      insertGrade(dsl, 12L, 7L);
      insertGrade(dsl, 13L, 8L);
      insertUserGrade(dsl, 11L, 21L, 40);
      insertUserGrade(dsl, 12L, 21L, 50);
      insertUserGrade(dsl, 13L, 21L, 60);

      Jooq jooq = mock(Jooq.class);
      when(jooq.dsl()).thenReturn(dsl);
      GradesSavePersistenceAdapter adapter =
          new GradesSavePersistenceAdapter(
              jooq, Mappers.getMapper(GradesSavePersistenceMapper.class));

      CourseGrade currentGrades = new CourseGrade();
      currentGrades.setCourseId(7L);
      currentGrades.setGradeItems(java.util.List.of(gradeItem(11L), gradeItem(14L)));
      currentGrades.setUserGrades(
          java.util.List.of(userGrade(11L, 21L, 75), userGrade(14L, 21L, 90)));

      adapter.saveCourseGradeItemsPersistence(currentGrades);

      assertTrue(dsl.selectFrom(GRADES).where(GRADES.ID.eq(11L)).fetchOne().getActive());
      assertFalse(dsl.selectFrom(GRADES).where(GRADES.ID.eq(12L)).fetchOne().getActive());
      assertTrue(dsl.selectFrom(GRADES).where(GRADES.ID.eq(13L)).fetchOne().getActive());
      assertTrue(dsl.selectFrom(GRADES).where(GRADES.ID.eq(14L)).fetchOne().getActive());

      assertEquals(
          0,
          dsl.selectFrom(USERS_GRADES)
              .where(USERS_GRADES.GRADE_ID.eq(11L))
              .fetchOne()
              .getGrade()
              .compareTo(BigDecimal.valueOf(75)));
      assertFalse(
          dsl.selectFrom(USERS_GRADES).where(USERS_GRADES.GRADE_ID.eq(12L)).fetchOne().getActive());
      assertTrue(
          dsl.selectFrom(USERS_GRADES).where(USERS_GRADES.GRADE_ID.eq(13L)).fetchOne().getActive());
      assertTrue(
          dsl.selectFrom(USERS_GRADES).where(USERS_GRADES.GRADE_ID.eq(14L)).fetchOne().getActive());

      CourseGrade emptyGrades = new CourseGrade();
      emptyGrades.setCourseId(7L);
      emptyGrades.setGradeItems(java.util.List.of());
      emptyGrades.setUserGrades(java.util.List.of());

      adapter.saveCourseGradeItemsPersistence(emptyGrades);

      assertFalse(dsl.selectFrom(GRADES).where(GRADES.ID.eq(11L)).fetchOne().getActive());
      assertFalse(dsl.selectFrom(GRADES).where(GRADES.ID.eq(14L)).fetchOne().getActive());
      assertFalse(
          dsl.selectFrom(USERS_GRADES).where(USERS_GRADES.GRADE_ID.eq(11L)).fetchOne().getActive());
      assertTrue(dsl.selectFrom(GRADES).where(GRADES.ID.eq(13L)).fetchOne().getActive());
      assertTrue(
          dsl.selectFrom(USERS_GRADES).where(USERS_GRADES.GRADE_ID.eq(13L)).fetchOne().getActive());
    }
  }

  private void createTables(DSLContext dsl) {
    dsl.execute(
        """
        CREATE TABLE GRADES (
          ID BIGINT PRIMARY KEY,
          PARENT_ID BIGINT,
          COURSE_ID BIGINT NOT NULL,
          NAME VARCHAR(255),
          ITEM_TYPE VARCHAR(50),
          GRADE_MIN DECIMAL(15, 5),
          GRADE_MAX DECIMAL(15, 5),
          ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
          CREATED_AT BIGINT NOT NULL DEFAULT (unixepoch()),
          UPDATED_AT BIGINT NOT NULL DEFAULT (unixepoch())
        )
        """);
    dsl.execute(
        """
        CREATE TABLE USERS_GRADES (
          GRADE_ID BIGINT NOT NULL,
          USER_ID BIGINT NOT NULL,
          GRADE DECIMAL(15, 5),
          PERCENTAGE DECIMAL(5, 2),
          ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
          CREATED_AT BIGINT NOT NULL DEFAULT (unixepoch()),
          UPDATED_AT BIGINT NOT NULL DEFAULT (unixepoch()),
          PRIMARY KEY (GRADE_ID, USER_ID)
        )
        """);
  }

  private void insertGrade(DSLContext dsl, long gradeId, long courseId) {
    dsl.insertInto(GRADES)
        .set(GRADES.ID, gradeId)
        .set(GRADES.COURSE_ID, courseId)
        .set(GRADES.ACTIVE, true)
        .execute();
  }

  private void insertUserGrade(DSLContext dsl, long gradeId, long userId, int grade) {
    dsl.insertInto(USERS_GRADES)
        .set(USERS_GRADES.GRADE_ID, gradeId)
        .set(USERS_GRADES.USER_ID, userId)
        .set(USERS_GRADES.GRADE, BigDecimal.valueOf(grade))
        .set(USERS_GRADES.ACTIVE, true)
        .execute();
  }

  private GradeItem gradeItem(long id) {
    GradeItem gradeItem = new GradeItem();
    gradeItem.setId(id);
    gradeItem.setName("Grade " + id);
    gradeItem.setGradeMin(BigDecimal.ZERO);
    gradeItem.setGradeMax(BigDecimal.valueOf(100));
    return gradeItem;
  }

  private UserGrade userGrade(long gradeItemId, long userId, int grade) {
    UserGrade userGrade = new UserGrade();
    userGrade.setGradeItemId(gradeItemId);
    userGrade.setUserId(userId);
    userGrade.setGradeRaw(BigDecimal.valueOf(grade));
    userGrade.setGradePercentageRaw(BigDecimal.valueOf(grade));
    return userGrade;
  }
}
