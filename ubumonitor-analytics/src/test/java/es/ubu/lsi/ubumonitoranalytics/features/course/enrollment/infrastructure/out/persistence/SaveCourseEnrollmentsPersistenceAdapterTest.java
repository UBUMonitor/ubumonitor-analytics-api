package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourseGroups.USERS_COURSE_GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersRoles.USERS_ROLES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.EnrollmentWriteMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.DatabaseUtil;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class SaveCourseEnrollmentsPersistenceAdapterTest {

  @TempDir Path tempDir;

  @Test
  void emptyInputDeactivatesOnlyMembershipsInTheSynchronizedCourse() throws Exception {
    withDatabase(
        "empty-sync.db",
        dsl -> {
          insertCourse(dsl, 2L);
          insertCourse(dsl, 3L);
          insertCourseEnrollment(dsl, 13L, 2L, 100L, true);
          insertCourseEnrollment(dsl, 13L, 3L, 200L, true);
          insertGroupMembership(dsl, 13L, 7L, 2L, true);
          insertGroupMembership(dsl, 13L, 8L, 3L, true);
          insertRoleMembership(dsl, 13L, 5L, 2L, true);
          insertRoleMembership(dsl, 13L, 6L, 3L, true);

          adapter(dsl).save(CourseEnrollments.empty(2L));

          assertFalse(courseEnrollment(dsl, 13L, 2L).getActive());
          assertTrue(courseEnrollment(dsl, 13L, 3L).getActive());
          assertFalse(groupMembership(dsl, 13L, 7L, 2L).getActive());
          assertTrue(groupMembership(dsl, 13L, 8L, 3L).getActive());
          assertFalse(roleMembership(dsl, 13L, 5L, 2L).getActive());
          assertTrue(roleMembership(dsl, 13L, 6L, 3L).getActive());
          assertNotNull(course(dsl, 2L).getLastSync());
        });
  }

  @Test
  void duplicateEnrollmentsPersistOneRowForEachMembership() throws Exception {
    withDatabase(
        "duplicate-enrollments.db",
        dsl -> {
          insertCourse(dsl, 2L);
          Course course = courseModel(2L);
          User user = user(13L);
          Group group = new Group(7L, "Group", null);
          Role role = new Role(5L, "Student");
          Enrollment enrollment =
              new Enrollment(
                  user, List.of(role), List.of(group), List.of(new UserCourse(course, 100L)));

          adapter(dsl).save(new CourseEnrollments(2L, List.of(enrollment, enrollment)));

          assertEquals(1, dsl.fetchCount(USERS, USERS.ID.eq(13L)));
          assertEquals(1, dsl.fetchCount(USERS_COURSES, USERS_COURSES.USER_ID.eq(13L)));
          assertEquals(1, dsl.fetchCount(USERS_COURSE_GROUPS, USERS_COURSE_GROUPS.USER_ID.eq(13L)));
          assertEquals(1, dsl.fetchCount(USERS_ROLES, USERS_ROLES.USER_ID.eq(13L)));
        });
  }

  @Test
  void synchronizationReactivatesReportedRelationsAndDeactivatesMissingOnes() throws Exception {
    withDatabase(
        "reactivate-relations.db",
        dsl -> {
          insertCourse(dsl, 2L);
          insertUser(dsl, 13L);
          insertUser(dsl, 14L);
          insertCourseEnrollment(dsl, 13L, 2L, 100L, false);
          insertCourseEnrollment(dsl, 14L, 2L, 200L, true);
          insertGroupMembership(dsl, 13L, 7L, 2L, false);
          insertGroupMembership(dsl, 14L, 8L, 2L, true);
          insertRoleMembership(dsl, 13L, 5L, 2L, false);
          insertRoleMembership(dsl, 14L, 6L, 2L, true);

          adapter(dsl)
              .save(
                  new CourseEnrollments(
                      2L,
                      List.of(
                          new Enrollment(
                              user(13L),
                              List.of(new Role(5L, "Student")),
                              List.of(new Group(7L, "Group", null)),
                              List.of(new UserCourse(courseModel(2L), 300L))))));

          assertTrue(courseEnrollment(dsl, 13L, 2L).getActive());
          assertFalse(courseEnrollment(dsl, 14L, 2L).getActive());
          assertTrue(groupMembership(dsl, 13L, 7L, 2L).getActive());
          assertFalse(groupMembership(dsl, 14L, 8L, 2L).getActive());
          assertTrue(roleMembership(dsl, 13L, 5L, 2L).getActive());
          assertFalse(roleMembership(dsl, 14L, 6L, 2L).getActive());
        });
  }

  @Test
  void nullLastCourseAccessPreservesThePreviouslyStoredAccess() throws Exception {
    withDatabase(
        "null-last-access.db",
        dsl -> {
          insertCourse(dsl, 2L);
          insertCourseEnrollment(dsl, 13L, 2L, 777L, true);

          adapter(dsl)
              .save(
                  new CourseEnrollments(
                      2L,
                      List.of(
                          new Enrollment(
                              user(13L),
                              List.of(),
                              List.of(),
                              List.of(new UserCourse(courseModel(2L), null))))));

          assertEquals(777L, courseEnrollment(dsl, 13L, 2L).getLastCourseAccess());
        });
  }

  private void withDatabase(String fileName, DatabaseAction action) throws Exception {
    String jdbcUrl = "jdbc:sqlite:" + tempDir.resolve(fileName).toString().replace("\\", "/");
    try (Connection connection =
        DatabaseUtil.createDataSource(jdbcUrl, "test-key").getConnection()) {
      DSLContext dsl = DSL.using(connection, SQLDialect.SQLITE);
      ScriptUtils.executeSqlScript(
          connection, new ClassPathResource("db/test/course-enrollments-schema.sql"));
      action.run(dsl);
    }
  }

  private SaveCourseEnrollmentsPersistenceAdapter adapter(DSLContext dsl) {
    Jooq jooq = mock(Jooq.class);
    when(jooq.dsl()).thenReturn(dsl);
    return new SaveCourseEnrollmentsPersistenceAdapter(
        jooq, Mappers.getMapper(EnrollmentWriteMapper.class));
  }

  private void insertCourse(DSLContext dsl, long id) {
    dsl.insertInto(COURSES).set(COURSES.ID, id).execute();
  }

  private void insertUser(DSLContext dsl, long id) {
    dsl.insertInto(USERS).set(USERS.ID, id).execute();
  }

  private void insertCourseEnrollment(
      DSLContext dsl, long userId, long courseId, long lastAccess, boolean active) {
    dsl.insertInto(USERS_COURSES)
        .set(USERS_COURSES.USER_ID, userId)
        .set(USERS_COURSES.COURSE_ID, courseId)
        .set(USERS_COURSES.LAST_COURSE_ACCESS, lastAccess)
        .set(USERS_COURSES.ACTIVE, active)
        .execute();
  }

  private void insertGroupMembership(
      DSLContext dsl, long userId, long groupId, long courseId, boolean active) {
    dsl.insertInto(USERS_COURSE_GROUPS)
        .set(USERS_COURSE_GROUPS.USER_ID, userId)
        .set(USERS_COURSE_GROUPS.GROUP_ID, groupId)
        .set(USERS_COURSE_GROUPS.COURSE_ID, courseId)
        .set(USERS_COURSE_GROUPS.ACTIVE, active)
        .execute();
  }

  private void insertRoleMembership(
      DSLContext dsl, long userId, long roleId, long courseId, boolean active) {
    dsl.insertInto(USERS_ROLES)
        .set(USERS_ROLES.USER_ID, userId)
        .set(USERS_ROLES.ROLE_ID, roleId)
        .set(USERS_ROLES.COURSE_ID, courseId)
        .set(USERS_ROLES.ACTIVE, active)
        .execute();
  }

  private es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord courseEnrollment(
      DSLContext dsl, long userId, long courseId) {
    return Objects.requireNonNull(
        dsl.selectFrom(USERS_COURSES)
            .where(USERS_COURSES.USER_ID.eq(userId).and(USERS_COURSES.COURSE_ID.eq(courseId)))
            .fetchOne());
  }

  private es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCourseGroupsRecord
      groupMembership(DSLContext dsl, long userId, long groupId, long courseId) {
    return Objects.requireNonNull(
        dsl.selectFrom(USERS_COURSE_GROUPS)
            .where(
                USERS_COURSE_GROUPS
                    .USER_ID
                    .eq(userId)
                    .and(USERS_COURSE_GROUPS.GROUP_ID.eq(groupId))
                    .and(USERS_COURSE_GROUPS.COURSE_ID.eq(courseId)))
            .fetchOne());
  }

  private es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRolesRecord roleMembership(
      DSLContext dsl, long userId, long roleId, long courseId) {
    return Objects.requireNonNull(
        dsl.selectFrom(USERS_ROLES)
            .where(
                USERS_ROLES
                    .USER_ID
                    .eq(userId)
                    .and(USERS_ROLES.ROLE_ID.eq(roleId))
                    .and(USERS_ROLES.COURSE_ID.eq(courseId)))
            .fetchOne());
  }

  private es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord course(
      DSLContext dsl, long courseId) {
    return Objects.requireNonNull(
        dsl.selectFrom(COURSES).where(COURSES.ID.eq(courseId)).fetchOne());
  }

  private User user(long id) {
    return new User(id, "User " + id, "First", "Last", null, null, null, null);
  }

  private Course courseModel(long id) {
    return new Course(id, "Course " + id, "C" + id, null);
  }

  @FunctionalInterface
  private interface DatabaseAction {
    void run(DSLContext dsl) throws Exception;
  }
}
