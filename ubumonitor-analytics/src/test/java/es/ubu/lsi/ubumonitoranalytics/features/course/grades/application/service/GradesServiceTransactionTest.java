package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.service;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Grades.GRADES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGrades.USERS_GRADES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.in.GradesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesFetchPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.GradesSavePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence.GradesSavePersistenceAdapter;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence.GradesSavePersistenceMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GradesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGradesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringJUnitConfig(GradesServiceTransactionTest.TestConfig.class)
class GradesServiceTransactionTest {

  @Autowired private GradesUseCase service;
  @Autowired private GradesApiPort gradesApiPort;
  @Autowired private DSLContext dsl;
  @Autowired private GradesSavePersistenceMapper mapper;

  private final List<String> callOrder = new ArrayList<>();

  @BeforeEach
  void createGradesTables() {
    dsl.execute("DROP TRIGGER IF EXISTS fail_grade_update");
    dsl.execute("DROP TABLE IF EXISTS users_grades");
    dsl.execute("DROP TABLE IF EXISTS grades");
    dsl.execute(
        """
        CREATE TABLE grades (
          id BIGINT PRIMARY KEY,
          parent_id BIGINT,
          category_id BIGINT,
          parent_category_id BIGINT,
          course_id BIGINT NOT NULL,
          name VARCHAR(255),
          item_type VARCHAR(50),
          module_id BIGINT,
          position INTEGER,
          grade_min DECIMAL(15, 5),
          grade_max DECIMAL(15, 5),
          active BOOLEAN NOT NULL DEFAULT TRUE,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE users_grades (
          grade_id BIGINT NOT NULL,
          user_id BIGINT NOT NULL,
          grade DECIMAL(15, 5),
          percentage DECIMAL(5, 2),
          active BOOLEAN NOT NULL DEFAULT TRUE,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0,
          PRIMARY KEY (grade_id, user_id)
        )
        """);

    callOrder.clear();
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("persist");
              GradeItem gradeItem = invocation.getArgument(0);
              Long courseId = invocation.getArgument(1);
              Long now = invocation.getArgument(2);
              GradesRecord record = dsl.newRecord(GRADES);
              record.setId(gradeItem.getId());
              record.setCourseId(courseId);
              record.setName(gradeItem.getName());
              record.setActive(true);
              record.setUpdatedAt(now);
              return record;
            })
        .when(mapper)
        .toRecord(any(GradeItem.class), any(Long.class), any(Long.class));
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("persist");
              UserGrade userGrade = invocation.getArgument(0);
              Long now = invocation.getArgument(1);
              UsersGradesRecord record = dsl.newRecord(USERS_GRADES);
              record.setGradeId(userGrade.getGradeItemId());
              record.setUserId(userGrade.getUserId());
              record.setGrade(userGrade.getGradeRaw());
              record.setPercentage(userGrade.getGradePercentageRaw());
              record.setActive(true);
              record.setUpdatedAt(now);
              return record;
            })
        .when(mapper)
        .toRecord(any(UserGrade.class), any(Long.class));
  }

  @Test
  void fetchesFromMoodleBeforeStartingPersistenceTransaction() {
    CourseGrade result = courseGrade("Fetched grade", 80);
    when(gradesApiPort.fetchCourseGradeItems(7L, 19L))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return result;
            });

    assertEquals(List.of(result.getGradeItems().getFirst()), service.sync(7L));
    assertEquals(List.of("moodle", "persist", "persist"), callOrder);
    assertEquals("Fetched grade", dsl.select(GRADES.NAME).from(GRADES).fetchOne(GRADES.NAME));
  }

  @Test
  void failedPersistenceDoesNotLeavePartiallyUpdatedGradeData() {
    insertExistingGrade("Existing grade", 75);
    dsl.execute(
        """
        CREATE TRIGGER fail_grade_update BEFORE UPDATE ON users_grades
        WHEN NEW.grade = 99
        BEGIN
          SELECT RAISE(ABORT, 'forced persistence failure');
        END
        """);
    when(gradesApiPort.fetchCourseGradeItems(7L, 19L))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return courseGrade("Replacement grade", 99);
            });

    assertThrows(RuntimeException.class, () -> service.sync(7L));

    assertEquals(List.of("moodle", "persist", "persist"), callOrder);
    assertEquals("Existing grade", dsl.select(GRADES.NAME).from(GRADES).fetchOne(GRADES.NAME));
    assertTrue(dsl.select(GRADES.ACTIVE).from(GRADES).fetchOne(GRADES.ACTIVE));
    assertEquals(
        0,
        dsl.select(USERS_GRADES.GRADE)
            .from(USERS_GRADES)
            .fetchOne(USERS_GRADES.GRADE)
            .compareTo(BigDecimal.valueOf(75)));
    assertTrue(dsl.select(USERS_GRADES.ACTIVE).from(USERS_GRADES).fetchOne(USERS_GRADES.ACTIVE));
  }

  private void insertExistingGrade(String name, int grade) {
    dsl.insertInto(GRADES)
        .set(GRADES.ID, 11L)
        .set(GRADES.COURSE_ID, 7L)
        .set(GRADES.NAME, name)
        .set(GRADES.ACTIVE, true)
        .execute();
    dsl.insertInto(USERS_GRADES)
        .set(USERS_GRADES.GRADE_ID, 11L)
        .set(USERS_GRADES.USER_ID, 19L)
        .set(USERS_GRADES.GRADE, BigDecimal.valueOf(grade))
        .set(USERS_GRADES.ACTIVE, true)
        .execute();
  }

  private CourseGrade courseGrade(String name, int grade) {
    GradeItem gradeItem = new GradeItem();
    gradeItem.setId(11L);
    gradeItem.setName(name);
    UserGrade userGrade = new UserGrade();
    userGrade.setGradeItemId(11L);
    userGrade.setUserId(19L);
    userGrade.setGradeRaw(BigDecimal.valueOf(grade));
    userGrade.setGradePercentageRaw(BigDecimal.valueOf(grade));
    CourseGrade courseGrade = new CourseGrade();
    courseGrade.setCourseId(7L);
    courseGrade.setGradeItems(List.of(gradeItem));
    courseGrade.setUserGrades(List.of(userGrade));
    return courseGrade;
  }

  @Configuration
  @EnableTransactionManagement
  static class TestConfig {

    @Bean
    SingleConnectionDataSource dataSource() {
      return new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
    }

    @Bean
    PlatformTransactionManager transactionManager(SingleConnectionDataSource dataSource) {
      return new JdbcTransactionManager(dataSource);
    }

    @Bean
    DSLContext dsl(SingleConnectionDataSource dataSource) {
      return DSL.using(new TransactionAwareDataSourceProxy(dataSource), SQLDialect.SQLITE);
    }

    @Bean
    Jooq jooq(DSLContext dsl) {
      Jooq jooq = org.mockito.Mockito.mock(Jooq.class);
      when(jooq.dsl()).thenReturn(dsl);
      return jooq;
    }

    @Bean
    GradesSavePersistenceMapper mapper() {
      return org.mockito.Mockito.mock(GradesSavePersistenceMapper.class);
    }

    @Bean
    GradesApiPort gradesApiPort() {
      return org.mockito.Mockito.mock(GradesApiPort.class);
    }

    @Bean
    GradesFetchPersistencePort gradesFetchPersistencePort() {
      return org.mockito.Mockito.mock(GradesFetchPersistencePort.class);
    }

    @Bean
    CurrentSessionPort currentSessionPort() {
      CurrentSessionPort currentSessionPort = org.mockito.Mockito.mock(CurrentSessionPort.class);
      when(currentSessionPort.getSessionData())
          .thenReturn(SessionData.builder().userId(19L).build());
      return currentSessionPort;
    }

    @Bean
    GradesSavePersistenceAdapter gradesSavePersistenceAdapter(
        Jooq jooq, GradesSavePersistenceMapper mapper) {
      return new GradesSavePersistenceAdapter(jooq, mapper);
    }

    @Bean
    GradesService gradesService(
        GradesFetchPersistencePort fetchPersistencePort,
        GradesSavePersistencePort savePersistencePort,
        CurrentSessionPort currentSessionPort,
        GradesApiPort gradesApiPort) {
      return new GradesService(
          fetchPersistencePort, savePersistencePort, currentSessionPort, gradesApiPort);
    }
  }
}
