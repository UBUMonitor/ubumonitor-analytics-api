package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.EnrollmentReadMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;

class LoadCourseEnrollmentsPersistenceAdapterTest {

  @TempDir Path tempDir;

  @Test
  void fetchesEnrollmentEntitiesSeparatelyAndLimitsGroupsAndRolesToRequestedCourse()
      throws Exception {
    String jdbcUrl =
        "jdbc:sqlite:" + tempDir.resolve("course-enrollments.db").toString().replace("\\", "/");
    try (var connection = DatabaseUtil.createDataSource(jdbcUrl, "test-key").getConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);
      createTables(dsl);
      insertCourse(dsl, 2L, "Requested course");
      insertCourse(dsl, 3L, "Other course");
      insertUser(dsl, 13L, "First user");
      insertUser(dsl, 14L, "Second user");
      insertCourseEnrollment(dsl, 13L, 2L, 100L);
      insertCourseEnrollment(dsl, 13L, 3L, 200L);
      insertCourseEnrollment(dsl, 14L, 2L, 300L);
      insertGroup(dsl, 7L, 2L, "Requested group");
      insertGroup(dsl, 8L, 3L, "Other group");
      insertRole(dsl, 5L, "Requested role");
      insertRole(dsl, 6L, "Other role");
      insertGroupMembership(dsl, 13L, 7L, 2L);
      insertGroupMembership(dsl, 13L, 8L, 3L);
      insertRoleMembership(dsl, 13L, 5L, 2L);
      insertRoleMembership(dsl, 13L, 6L, 3L);

      Jooq jooq = mock(Jooq.class);
      when(jooq.dsl()).thenReturn(dsl);
      LoadCourseEnrollmentsPersistenceAdapter adapter =
          new LoadCourseEnrollmentsPersistenceAdapter(
              jooq, Mappers.getMapper(EnrollmentReadMapper.class));

      CourseEnrollments response = adapter.load(2L);

      assertEquals(
          Set.of(13L, 14L), response.users().stream().map(User::getId).collect(Collectors.toSet()));
      assertEquals(
          Set.of(2L, 3L),
          response.courses().stream().map(course -> course.getId()).collect(Collectors.toSet()));
      assertEquals(List.of(7L), response.groups().stream().map(group -> group.getId()).toList());
      assertEquals(List.of(5L), response.roles().stream().map(role -> role.getId()).toList());

      Enrollment firstEnrollment =
          response.getEnrollments().stream()
              .filter(enrollment -> enrollment.getUser().getId().equals(13L))
              .findFirst()
              .orElseThrow();
      assertEquals(
          Set.of(2L, 3L),
          firstEnrollment.getCourses().stream()
              .map(userCourse -> userCourse.getCourse().getId())
              .collect(Collectors.toSet()));
      assertEquals(
          List.of(7L), firstEnrollment.getGroups().stream().map(group -> group.getId()).toList());
      assertEquals(
          List.of(5L), firstEnrollment.getRoles().stream().map(role -> role.getId()).toList());
      assertEquals(100L, firstEnrollment.lastAccessTo(2L));
    }
  }

  private void createTables(DSLContext dsl) {
    dsl.execute(
        """
        CREATE TABLE USERS (
          ID BIGINT PRIMARY KEY,
          FULL_NAME VARCHAR(255),
          FIRST_NAME VARCHAR(255),
          LAST_NAME VARCHAR(255),
          EMAIL VARCHAR(255),
          FIRST_ACCESS BIGINT,
          LAST_ACCESS BIGINT,
          CREATED_AT BIGINT NOT NULL DEFAULT 0,
          UPDATED_AT BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE COURSES (
          ID BIGINT PRIMARY KEY,
          FULL_NAME VARCHAR(255),
          SHORT_NAME VARCHAR(255),
          SHOW_GRADES BOOLEAN NOT NULL DEFAULT 0,
          ENABLE_COMPLETION BOOLEAN NOT NULL DEFAULT 0,
          START_DATE BIGINT,
          END_DATE BIGINT,
          TIME_MODIFIED BIGINT,
          LAST_SYNC BIGINT,
          CREATED_AT BIGINT NOT NULL DEFAULT 0,
          UPDATED_AT BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE USERS_COURSES (
          USER_ID BIGINT NOT NULL,
          COURSE_ID BIGINT NOT NULL,
          LAST_COURSE_ACCESS BIGINT,
          IS_FAVOURITE BOOLEAN NOT NULL DEFAULT 0,
          ACTIVE BOOLEAN NOT NULL DEFAULT 1,
          CREATED_AT BIGINT NOT NULL DEFAULT 0,
          UPDATED_AT BIGINT NOT NULL DEFAULT 0,
          PRIMARY KEY (USER_ID, COURSE_ID)
        )
        """);
    dsl.execute(
        """
        CREATE TABLE GROUPS (
          ID BIGINT PRIMARY KEY,
          COURSE_ID BIGINT NOT NULL,
          NAME VARCHAR(255),
          DESCRIPTION TEXT,
          CREATED_AT BIGINT NOT NULL DEFAULT 0,
          UPDATED_AT BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE USERS_GROUPS (
          USER_ID BIGINT NOT NULL,
          GROUP_ID BIGINT NOT NULL,
          COURSE_ID BIGINT NOT NULL,
          ACTIVE BOOLEAN NOT NULL DEFAULT 1,
          CREATED_AT BIGINT NOT NULL DEFAULT 0,
          UPDATED_AT BIGINT NOT NULL DEFAULT 0,
          PRIMARY KEY (USER_ID, GROUP_ID, COURSE_ID)
        )
        """);
    dsl.execute(
        """
        CREATE TABLE ROLES (
          ID BIGINT PRIMARY KEY,
          NAME VARCHAR(255),
          CREATED_AT BIGINT NOT NULL DEFAULT 0,
          UPDATED_AT BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE USERS_ROLES (
          ROLE_ID BIGINT NOT NULL,
          USER_ID BIGINT NOT NULL,
          COURSE_ID BIGINT NOT NULL,
          ACTIVE BOOLEAN NOT NULL DEFAULT 1,
          CREATED_AT BIGINT NOT NULL DEFAULT 0,
          UPDATED_AT BIGINT NOT NULL DEFAULT 0,
          PRIMARY KEY (USER_ID, ROLE_ID, COURSE_ID)
        )
        """);
  }

  private void insertCourse(DSLContext dsl, long id, String name) {
    dsl.execute("INSERT INTO COURSES (ID, FULL_NAME) VALUES ({0}, {1})", id, name);
  }

  private void insertUser(DSLContext dsl, long id, String name) {
    dsl.execute("INSERT INTO USERS (ID, FULL_NAME) VALUES ({0}, {1})", id, name);
  }

  private void insertCourseEnrollment(
      DSLContext dsl, long userId, long courseId, long lastCourseAccess) {
    dsl.execute(
        """
        INSERT INTO USERS_COURSES (USER_ID, COURSE_ID, LAST_COURSE_ACCESS, ACTIVE)
        VALUES ({0}, {1}, {2}, TRUE)
        """,
        userId, courseId, lastCourseAccess);
  }

  private void insertGroup(DSLContext dsl, long id, long courseId, String name) {
    dsl.execute(
        "INSERT INTO GROUPS (ID, COURSE_ID, NAME) VALUES ({0}, {1}, {2})", id, courseId, name);
  }

  private void insertRole(DSLContext dsl, long id, String name) {
    dsl.execute("INSERT INTO ROLES (ID, NAME) VALUES ({0}, {1})", id, name);
  }

  private void insertGroupMembership(DSLContext dsl, long userId, long groupId, long courseId) {
    dsl.execute(
        """
        INSERT INTO USERS_GROUPS (USER_ID, GROUP_ID, COURSE_ID, ACTIVE)
        VALUES ({0}, {1}, {2}, TRUE)
        """,
        userId, groupId, courseId);
  }

  private void insertRoleMembership(DSLContext dsl, long userId, long roleId, long courseId) {
    dsl.execute(
        """
        INSERT INTO USERS_ROLES (USER_ID, ROLE_ID, COURSE_ID, ACTIVE)
        VALUES ({0}, {1}, {2}, TRUE)
        """,
        userId, roleId, courseId);
  }
}
