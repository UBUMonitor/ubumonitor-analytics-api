package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.service;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out.EnrollmentCoursesApiFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out.PersistenceFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out.SyncEnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.persistence.SyncSyncEnrollmentPersistenceAdapter;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.persistence.mapper.SyncEnrollmentPersistenceAdapterMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
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

@SpringJUnitConfig(SyncUserCoursesServiceTransactionTest.TestConfig.class)
class SyncUserCoursesServiceTransactionTest {

  @Autowired private SyncUserCoursesService service;
  @Autowired private CurrentSessionPort currentSessionPort;
  @Autowired private EnrollmentCoursesApiFetchPort moodlePort;
  @Autowired private DSLContext dsl;
  @Autowired private SyncEnrollmentPersistenceAdapterMapper mapper;

  private final List<String> callOrder = new ArrayList<>();

  @BeforeEach
  void createEnrollmentTables() {
    dsl.execute("DROP TRIGGER IF EXISTS fail_enrollment_insert");
    dsl.execute("DROP TABLE IF EXISTS users_courses");
    dsl.execute("DROP TABLE IF EXISTS courses");
    dsl.execute("DROP TABLE IF EXISTS users");
    dsl.execute(
        """
        CREATE TABLE users (
          id BIGINT PRIMARY KEY,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE courses (
          id BIGINT PRIMARY KEY,
          full_name TEXT,
          short_name TEXT,
          show_grades BOOLEAN NOT NULL DEFAULT FALSE,
          enable_completion BOOLEAN NOT NULL DEFAULT FALSE,
          start_date BIGINT,
          end_date BIGINT,
          time_modified BIGINT,
          last_sync BIGINT,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE users_courses (
          user_id BIGINT NOT NULL,
          course_id BIGINT NOT NULL,
          last_course_access BIGINT,
          is_favourite BOOLEAN NOT NULL DEFAULT FALSE,
          active BOOLEAN NOT NULL DEFAULT TRUE,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0,
          PRIMARY KEY (user_id, course_id)
        )
        """);

    callOrder.clear();
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("persist");
              Course course = invocation.getArgument(0);
              CoursesRecord record = invocation.getArgument(1);
              record.setId(course.getId());
              record.setFullName(course.getFullName());
              record.setShortName(course.getShortName());
              record.setShowGrades(course.getShowGrades());
              record.setEnableCompletion(course.getEnableCompletion());
              record.setStartDate(course.getStartDate());
              record.setEndDate(course.getEndDate());
              record.setTimeModified(course.getTimeModified());
              return null;
            })
        .when(mapper)
        .toRecord(any(Course.class), any(CoursesRecord.class));
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("persist");
              Enrollment enrollment = invocation.getArgument(0);
              UsersCoursesRecord record = invocation.getArgument(1);
              record.setLastCourseAccess(enrollment.getLastCourseAccess());
              record.setIsFavourite(enrollment.getIsFavourite());
              return null;
            })
        .when(mapper)
        .toRecord(any(Enrollment.class), any(UsersCoursesRecord.class));
    when(currentSessionPort.getSessionData())
        .thenReturn(SessionData.builder().userId(19L).username("teacher").build());
  }

  @Test
  void fetchesFromMoodleBeforeStartingAtomicPersistence() {
    UserEnrolledCourses fetched = enrollment(2L, "Fetched course");
    when(moodlePort.fetchEnrolledCourses(19L))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return fetched;
            });

    assertEquals(fetched, service.syncActualUserEnrollments());

    assertEquals(List.of("moodle", "persist", "persist"), callOrder);
    assertEquals(
        "Fetched course",
        dsl.select(COURSES.FULL_NAME)
            .from(COURSES)
            .where(COURSES.ID.eq(2L))
            .fetchOne(COURSES.FULL_NAME));
    assertEquals(
        19L,
        dsl.select(USERS_COURSES.USER_ID)
            .from(USERS_COURSES)
            .where(USERS_COURSES.COURSE_ID.eq(2L))
            .fetchOne(USERS_COURSES.USER_ID));
  }

  @Test
  void failedPersistenceRollsBackAllEnrollmentChanges() {
    dsl.insertInto(COURSES).set(COURSES.ID, 1L).set(COURSES.FULL_NAME, "Existing course").execute();
    dsl.insertInto(USERS_COURSES)
        .set(USERS_COURSES.USER_ID, 19L)
        .set(USERS_COURSES.COURSE_ID, 1L)
        .set(USERS_COURSES.ACTIVE, true)
        .execute();
    dsl.execute(
        """
        CREATE TRIGGER fail_enrollment_insert BEFORE INSERT ON users_courses
        WHEN NEW.course_id = 2
        BEGIN
          SELECT RAISE(ABORT, 'forced persistence failure');
        END
        """);
    when(moodlePort.fetchEnrolledCourses(19L))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return enrollment(2L, "Replacement course");
            });

    assertThrows(RuntimeException.class, service::syncActualUserEnrollments);

    assertEquals(List.of("moodle", "persist", "persist"), callOrder);
    assertEquals(
        "Existing course",
        dsl.select(COURSES.FULL_NAME)
            .from(COURSES)
            .where(COURSES.ID.eq(1L))
            .fetchOne(COURSES.FULL_NAME));
    assertEquals(
        0, dsl.selectCount().from(COURSES).where(COURSES.ID.eq(2L)).fetchOne(0, int.class));
    assertTrue(
        dsl.select(USERS_COURSES.ACTIVE)
            .from(USERS_COURSES)
            .where(USERS_COURSES.USER_ID.eq(19L))
            .and(USERS_COURSES.COURSE_ID.eq(1L))
            .fetchOne(USERS_COURSES.ACTIVE));
    assertFalse(
        dsl.select(USERS_COURSES.ACTIVE)
            .from(USERS_COURSES)
            .where(USERS_COURSES.COURSE_ID.eq(2L))
            .fetchOptional()
            .isPresent());
  }

  private UserEnrolledCourses enrollment(Long courseId, String name) {
    Course course = new Course();
    course.setId(courseId);
    course.setFullName(name);
    course.setShortName(name);
    course.setShowGrades(true);
    course.setEnableCompletion(false);
    Enrollment enrollment = new Enrollment();
    enrollment.setCourse(course);
    enrollment.setLastCourseAccess(123L);
    enrollment.setIsFavourite(false);
    return UserEnrolledCourses.builder().userId(19L).enrolledCourses(List.of(enrollment)).build();
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
    CurrentSessionPort currentSessionPort() {
      return org.mockito.Mockito.mock(CurrentSessionPort.class);
    }

    @Bean
    EnrollmentCoursesApiFetchPort moodlePort() {
      return org.mockito.Mockito.mock(EnrollmentCoursesApiFetchPort.class);
    }

    @Bean
    PersistenceFetchPort persistenceFetchPort() {
      return org.mockito.Mockito.mock(PersistenceFetchPort.class);
    }

    @Bean
    SyncEnrollmentPersistenceAdapterMapper mapper() {
      return org.mockito.Mockito.mock(SyncEnrollmentPersistenceAdapterMapper.class);
    }

    @Bean
    SyncEnrollmentPersistencePort persistenceAdapter(
        Jooq jooq, SyncEnrollmentPersistenceAdapterMapper mapper) {
      return new SyncSyncEnrollmentPersistenceAdapter(jooq, mapper);
    }

    @Bean
    SyncUserCoursesService service(
        CurrentSessionPort currentSessionPort,
        EnrollmentCoursesApiFetchPort moodlePort,
        SyncEnrollmentPersistencePort persistenceAdapter,
        PersistenceFetchPort persistenceFetchPort) {
      return new SyncUserCoursesService(
          currentSessionPort, moodlePort, persistenceAdapter, persistenceFetchPort);
    }
  }
}
