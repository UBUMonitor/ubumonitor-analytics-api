package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Groups.GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Roles.ROLES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGroups.USERS_GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersImages.USERS_IMAGES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersRoles.USERS_ROLES;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.SaveCourseEnrollmentsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.EnrollmentWriteMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.RolesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersImagesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRolesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.UpdatableRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists course users, enrollments, roles and group memberships.
 *
 * <p>Memberships are replaced rather than patched: the ones of the synchronized course are first
 * deactivated and then reactivated by upserting what Moodle reported.
 */
@Component
@RequiredArgsConstructor
public class SaveCourseEnrollmentsPersistenceAdapter implements SaveCourseEnrollmentsPort {

  private final Jooq jooq;
  private final EnrollmentWriteMapper mapper;

  @Override
  @Transactional
  public void save(CourseEnrollments enrollments) {
    Long now = Instant.now().getEpochSecond();
    Long courseId = enrollments.getCourseId();
    List<User> users = enrollments.users();

    upsertUsers(users, now);
    upsertProfileImages(users, now);
    upsertCourses(enrollments.courses(), now);
    upsertGroups(enrollments.groups(), courseId, now);
    upsertRoles(enrollments.roles(), now);

    deactivateMemberships(courseId, enrollments.userIds(), now);

    upsertUserCourses(enrollments.getEnrollments(), now);
    upsertUserGroups(enrollments.getEnrollments(), courseId, now);
    upsertUserRoles(enrollments.getEnrollments(), courseId, now);

    markCourseSynced(courseId, now);
  }

  // ---------------------------------------------------------------- catalogs

  private void upsertUsers(List<User> users, Long now) {
    merge(
        users.stream()
            .map(
                user -> {
                  UsersRecord record = jooq.dsl().newRecord(USERS);
                  mapper.toUsersRecord(user, record);
                  record.setUpdatedAt(now);
                  return record;
                })
            .toList());
  }

  /** Users whose image could not be downloaded keep the image stored previously. */
  private void upsertProfileImages(List<User> users, Long now) {
    merge(
        users.stream()
            .filter(user -> hasImageData(user.getUserPicture()))
            .map(
                user -> {
                  UsersImagesRecord record = jooq.dsl().newRecord(USERS_IMAGES);
                  mapper.toUsersImagesRecord(user.getUserPicture(), record);
                  record.setUserId(user.getId());
                  record.setUpdatedAt(now);
                  return record;
                })
            .toList());
  }

  private static boolean hasImageData(UserPicture picture) {
    return picture != null && picture.getData() != null;
  }

  private void upsertCourses(List<Course> courses, Long now) {
    merge(
        courses.stream()
            .map(
                course -> {
                  CoursesRecord record = jooq.dsl().newRecord(COURSES);
                  mapper.toCoursesRecord(course, record);
                  record.setUpdatedAt(now);
                  return record;
                })
            .toList());
  }

  private void upsertGroups(List<Group> groups, Long courseId, Long now) {
    merge(
        groups.stream()
            .map(
                group -> {
                  GroupsRecord record = jooq.dsl().newRecord(GROUPS);
                  mapper.toGroupsRecord(group, record);
                  record.setCourseId(courseId);
                  record.setUpdatedAt(now);
                  return record;
                })
            .toList());
  }

  private void upsertRoles(List<Role> roles, Long now) {
    merge(
        roles.stream()
            .map(
                role -> {
                  RolesRecord record = jooq.dsl().newRecord(ROLES);
                  mapper.toRolesRecord(role, record);
                  record.setUpdatedAt(now);
                  return record;
                })
            .toList());
  }

  // ------------------------------------------------------------- memberships

  /**
   * Course enrollments are deactivated for every synchronized user (Moodle reports all their
   * courses) and for anyone else still active in this course, since they are no longer enrolled.
   * Groups and roles only exist within the synchronized course, so only that course is touched.
   */
  private void deactivateMemberships(Long courseId, List<Long> userIds, Long now) {
    jooq.dsl()
        .update(USERS_COURSES)
        .set(USERS_COURSES.ACTIVE, false)
        .set(USERS_COURSES.UPDATED_AT, now)
        .where(USERS_COURSES.USER_ID.in(userIds).or(USERS_COURSES.COURSE_ID.eq(courseId)))
        .and(USERS_COURSES.ACTIVE.isTrue())
        .execute();

    jooq.dsl()
        .update(USERS_GROUPS)
        .set(USERS_GROUPS.ACTIVE, false)
        .set(USERS_GROUPS.UPDATED_AT, now)
        .where(USERS_GROUPS.COURSE_ID.eq(courseId))
        .and(USERS_GROUPS.ACTIVE.isTrue())
        .execute();

    jooq.dsl()
        .update(USERS_ROLES)
        .set(USERS_ROLES.ACTIVE, false)
        .set(USERS_ROLES.UPDATED_AT, now)
        .where(USERS_ROLES.COURSE_ID.eq(courseId))
        .and(USERS_ROLES.ACTIVE.isTrue())
        .execute();
  }

  /**
   * A {@code null} last access means "unknown", not "never", so it is left out of the merge and the
   * stored value survives.
   */
  private void upsertUserCourses(List<Enrollment> enrollments, Long now) {
    merge(
        enrollments.stream()
            .flatMap(
                enrollment ->
                    enrollment.getCourses().stream()
                        .map(
                            userCourse ->
                                toUsersCoursesRecord(enrollment.getUser(), userCourse, now)))
            .toList());
  }

  private UsersCoursesRecord toUsersCoursesRecord(User user, UserCourse userCourse, Long now) {
    UsersCoursesRecord record = jooq.dsl().newRecord(USERS_COURSES);
    record.setUserId(user.getId());
    record.setCourseId(userCourse.getCourse().getId());
    record.setActive(true);
    record.setUpdatedAt(now);
    if (userCourse.getLastCourseAccess() != null) {
      record.setLastCourseAccess(userCourse.getLastCourseAccess());
    }
    return record;
  }

  private void upsertUserGroups(List<Enrollment> enrollments, Long courseId, Long now) {
    merge(
        enrollments.stream()
            .flatMap(
                enrollment ->
                    enrollment.getGroups().stream()
                        .map(
                            group -> {
                              UsersGroupsRecord record = jooq.dsl().newRecord(USERS_GROUPS);
                              record.setUserId(enrollment.getUser().getId());
                              record.setGroupId(group.getId());
                              record.setCourseId(courseId);
                              record.setActive(true);
                              record.setUpdatedAt(now);
                              return record;
                            }))
            .toList());
  }

  private void upsertUserRoles(List<Enrollment> enrollments, Long courseId, Long now) {
    merge(
        enrollments.stream()
            .flatMap(
                enrollment ->
                    enrollment.getRoles().stream()
                        .map(
                            role -> {
                              UsersRolesRecord record = jooq.dsl().newRecord(USERS_ROLES);
                              record.setUserId(enrollment.getUser().getId());
                              record.setRoleId(role.getId());
                              record.setCourseId(courseId);
                              record.setActive(true);
                              record.setUpdatedAt(now);
                              return record;
                            }))
            .toList());
  }

  private void markCourseSynced(Long courseId, Long now) {
    jooq.dsl()
        .update(COURSES)
        .set(COURSES.LAST_SYNC, now)
        .set(COURSES.UPDATED_AT, now)
        .where(COURSES.ID.eq(courseId))
        .execute();
  }

  private void merge(Collection<? extends UpdatableRecord<?>> records) {
    if (!records.isEmpty()) {
      jooq.dsl().batchMerge(records).execute();
    }
  }
}
