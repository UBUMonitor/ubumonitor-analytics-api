package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.*;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.EnrollmentReadMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class LoadCourseEnrollmentsPersistenceAdapterTest {

  @TempDir Path tempDir;

  @Test
  void fetchesEnrollmentEntitiesSeparatelyAndLimitsGroupsAndRolesToRequestedCourse()
      throws Exception {
    String jdbcUrl =
        "jdbc:sqlite:" + tempDir.resolve("course-enrollments.db").toString().replace("\\", "/");
    try (var connection = DatabaseUtil.createDataSource(jdbcUrl, "test-key").getConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);
      createTables(connection);
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
          response.courses().stream().map(Course::getId).collect(Collectors.toSet()));
      assertEquals(List.of(7L), response.groups().stream().map(Group::getId).toList());
      assertEquals(List.of(5L), response.roles().stream().map(Role::getId).toList());

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
      assertEquals(List.of(7L), firstEnrollment.getGroups().stream().map(Group::getId).toList());
      assertEquals(List.of(5L), firstEnrollment.getRoles().stream().map(Role::getId).toList());
      assertEquals(100L, firstEnrollment.lastAccessTo(2L));
    }
  }

  private void createTables(Connection connection) {
    ScriptUtils.executeSqlScript(
        connection, new ClassPathResource("db/test/course-enrollments-schema.sql"));
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
        "INSERT INTO COURSE_GROUPS (ID, COURSE_ID, NAME) VALUES ({0}, {1}, {2})",
        id, courseId, name);
  }

  private void insertRole(DSLContext dsl, long id, String name) {
    dsl.execute("INSERT INTO ROLES (ID, NAME) VALUES ({0}, {1})", id, name);
  }

  private void insertGroupMembership(DSLContext dsl, long userId, long groupId, long courseId) {
    dsl.execute(
        """
        INSERT INTO USERS_COURSE_GROUPS (USER_ID, GROUP_ID, COURSE_ID, ACTIVE)
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
