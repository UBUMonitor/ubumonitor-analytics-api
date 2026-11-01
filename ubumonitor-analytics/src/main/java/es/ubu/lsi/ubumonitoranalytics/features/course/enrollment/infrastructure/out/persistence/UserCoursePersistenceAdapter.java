package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Groups.GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Roles.ROLES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGroups.USERS_GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersImages.USERS_IMAGES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersRoles.USERS_ROLES;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.*;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.UserCourseMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.*;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persists course users, enrollments, roles, and group associations. */
@Component
@RequiredArgsConstructor
public class UserCoursePersistenceAdapter implements UserCoursePersistencePort {

  private final Jooq jooq;
  private final UserCourseMapper mapper;

  @Override
  public void sync(UsersResponse usersResponse) {
    Long now = Instant.now().getEpochSecond();

    upsertUsers(usersResponse, now);
    upsertCourses(usersResponse, now);
    upsertGroups(usersResponse, now);
    upsertRoles(usersResponse, now);

    List<Long> userIds = usersResponse.getUsers().stream().map(User::getId).toList();
    deactivateActualUsersCourses(userIds, now);
    deactivateActualUsersGroups(userIds, now);
    deactivateActualUsersRoles(userIds, now);

    upsertUsersCourses(usersResponse, now);
    upsertUsersGroups(usersResponse, now);
    upsertUsersRoles(usersResponse, now);

    setCourseLoaded(usersResponse, now);
  }

  private void upsertUsers(UsersResponse usersResponse, Long now) {
    List<UsersRecord> records =
        usersResponse.getUsers().stream().map(u -> toUsersRecord(u, now)).toList();

    jooq.dsl().batchMerge(records).execute();

    List<UsersImagesRecord> imagesRecords =
        usersResponse.getUsers().stream().map(u -> toUsersImagesRecord(u, now)).toList();

    jooq.dsl().batchMerge(imagesRecords).execute();
  }

  private UsersImagesRecord toUsersImagesRecord(User user, Long now) {
    UsersImagesRecord r = jooq.dsl().newRecord(USERS_IMAGES);

    mapper.toUsersImagesRecord(user.getUserPicture(), r);
    r.setUserId(user.getId());
    r.setUpdatedAt(now);
    return r;
  }

  public UsersRecord toUsersRecord(User user, Long now) {
    UsersRecord r = jooq.dsl().newRecord(USERS);
    mapper.toUsersRecord(user, r);
    r.setUpdatedAt(now);
    return r;
  }

  private void upsertCourses(UsersResponse usersResponse, Long now) {
    List<CoursesRecord> records =
        usersResponse.getUsers().stream()
            .map(User::getCourses)
            .flatMap(List::stream)
            .distinct()
            .map(c -> toCoursesRecord(c.getCourse(), now))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private CoursesRecord toCoursesRecord(Course course, Long now) {
    CoursesRecord r = jooq.dsl().newRecord(COURSES);
    mapper.toCoursesRecord(course, r);
    r.setUpdatedAt(now);
    return r;
  }

  private void upsertGroups(UsersResponse usersResponse, Long now) {

    List<GroupsRecord> records =
        usersResponse.getUsers().stream()
            .map(User::getGroups)
            .flatMap(List::stream)
            .distinct()
            .map(g -> toGroupsRecord(g, usersResponse.getActualCourseId(), now))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private GroupsRecord toGroupsRecord(Group group, Long courseId, Long now) {
    GroupsRecord r = jooq.dsl().newRecord(GROUPS);
    mapper.toGroupsRecord(group, r);
    r.setUpdatedAt(now);
    r.setCourseId(courseId);
    return r;
  }

  private void upsertRoles(UsersResponse usersResponse, Long now) {

    List<RolesRecord> records =
        usersResponse.getUsers().stream()
            .map(User::getRoles)
            .flatMap(List::stream)
            .distinct()
            .map(g -> toRolesRecord(g, now))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private RolesRecord toRolesRecord(Role role, Long now) {
    RolesRecord r = jooq.dsl().newRecord(ROLES);
    mapper.toRolesRecord(role, r);
    r.setUpdatedAt(now);
    return r;
  }

  private void deactivateActualUsersCourses(List<Long> userIds, Long now) {
    jooq.dsl()
        .update(USERS_COURSES)
        .set(USERS_COURSES.ACTIVE, false)
        .set(USERS_COURSES.UPDATED_AT, now)
        .where(USERS_COURSES.USER_ID.in(userIds))
        .and(USERS_COURSES.ACTIVE.eq(true))
        .execute();
  }

  private void deactivateActualUsersGroups(List<Long> userIds, Long now) {
    jooq.dsl()
        .update(USERS_GROUPS)
        .set(USERS_GROUPS.ACTIVE, false)
        .set(USERS_GROUPS.UPDATED_AT, now)
        .where(USERS_GROUPS.USER_ID.in(userIds))
        .and(USERS_GROUPS.ACTIVE.eq(true))
        .execute();
  }

  private void deactivateActualUsersRoles(List<Long> userIds, Long now) {
    jooq.dsl()
        .update(USERS_ROLES)
        .set(USERS_ROLES.ACTIVE, false)
        .set(USERS_ROLES.UPDATED_AT, now)
        .where(USERS_ROLES.USER_ID.in(userIds))
        .and(USERS_ROLES.ACTIVE.eq(true))
        .execute();
  }

  private void upsertUsersCourses(UsersResponse usersResponse, Long now) {

    List<UsersCoursesRecord> records =
        usersResponse.getUsers().stream()
            .flatMap(
                user ->
                    user.getCourses().stream()
                        .map(userCourse -> toUsersCoursesRecord(userCourse, user.getId(), now)))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private UsersCoursesRecord toUsersCoursesRecord(UserCourse userCourse, Long userId, Long now) {
    UsersCoursesRecord r = jooq.dsl().newRecord(USERS_COURSES);

    mapper.toUsersCoursesRecord(userCourse, r);
    r.setUpdatedAt(now);
    r.setActive(true);
    r.setCourseId(userCourse.getCourse().getId());
    r.setUserId(userId);
    return r;
  }

  private void upsertUsersGroups(UsersResponse usersResponse, Long now) {

    List<UsersGroupsRecord> records =
        usersResponse.getUsers().stream()
            .flatMap(
                user ->
                    user.getGroups().stream()
                        .map(
                            group ->
                                toUsersGroupsRecord(
                                    group, user.getId(), usersResponse.getActualCourseId(), now)))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private UsersGroupsRecord toUsersGroupsRecord(
      Group group, Long userId, Long actualCourseId, Long now) {
    UsersGroupsRecord r = jooq.dsl().newRecord(USERS_GROUPS);

    mapper.toUsersGroupsRecord(group, r);
    r.setUpdatedAt(now);
    r.setActive(true);
    r.setUserId(userId);
    r.setGroupId(group.getId());
    r.setCourseId(actualCourseId);

    return r;
  }

  private void upsertUsersRoles(UsersResponse usersResponse, Long now) {

    List<UsersRolesRecord> records =
        usersResponse.getUsers().stream()
            .flatMap(
                user ->
                    user.getRoles().stream()
                        .map(
                            role ->
                                toUsersRolesRecord(
                                    role, user.getId(), usersResponse.getActualCourseId(), now)))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private UsersRolesRecord toUsersRolesRecord(
      Role role, Long userId, Long actualCourseId, Long now) {
    UsersRolesRecord r = jooq.dsl().newRecord(USERS_ROLES);

    mapper.toUsersRolesRecord(role, r);
    r.setUpdatedAt(now);
    r.setActive(true);
    r.setUserId(userId);
    r.setRoleId(role.getId());
    r.setCourseId(actualCourseId);
    return r;
  }

  private void setCourseLoaded(UsersResponse usersResponse, Long now) {
    jooq.dsl()
        .update(COURSES)
        .set(COURSES.LAST_SYNC, now)
        .set(COURSES.UPDATED_AT, now)
        .where(COURSES.ID.eq(usersResponse.getActualCourseId()))
        .execute();
  }
}
