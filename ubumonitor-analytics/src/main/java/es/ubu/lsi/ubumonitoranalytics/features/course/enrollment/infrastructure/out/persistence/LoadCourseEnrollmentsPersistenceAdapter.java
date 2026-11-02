package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Groups.GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Roles.ROLES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGroups.USERS_GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersRoles.USERS_ROLES;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.LoadCourseEnrollmentsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.EnrollmentReadMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRolesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Reads course enrollments from tenant persistence. */
@Component
@RequiredArgsConstructor
public class LoadCourseEnrollmentsPersistenceAdapter implements LoadCourseEnrollmentsPort {

  private final Jooq jooq;
  private final EnrollmentReadMapper mapper;

  @Override
  public CourseEnrollments load(Long courseId) {
    List<Long> userIds = findActiveUserIds(courseId);
    if (userIds.isEmpty()) {
      return CourseEnrollments.empty(courseId);
    }

    Map<Long, User> users = findUsers(userIds);
    Map<Long, List<UserCourse>> coursesByUser = findCoursesByUser(userIds);
    Map<Long, List<Role>> rolesByUser = findRolesByUser(userIds, courseId);
    Map<Long, List<Group>> groupsByUser = findGroupsByUser(userIds, courseId);

    List<Enrollment> enrollments =
        userIds.stream()
            .filter(users::containsKey)
            .map(
                userId ->
                    new Enrollment(
                        users.get(userId),
                        rolesByUser.get(userId),
                        groupsByUser.get(userId),
                        coursesByUser.get(userId)))
            .toList();
    return new CourseEnrollments(courseId, enrollments);
  }

  private List<Long> findActiveUserIds(Long courseId) {
    return jooq.dsl()
        .select(USERS_COURSES.USER_ID)
        .from(USERS_COURSES)
        .where(USERS_COURSES.COURSE_ID.eq(courseId))
        .and(USERS_COURSES.ACTIVE.isTrue())
        .orderBy(USERS_COURSES.USER_ID)
        .fetch(USERS_COURSES.USER_ID);
  }

  private Map<Long, User> findUsers(List<Long> userIds) {
    return jooq.dsl()
        .selectFrom(USERS)
        .where(USERS.ID.in(userIds))
        .fetchMap(USERS.ID, mapper::toUser);
  }

  private Map<Long, List<UserCourse>> findCoursesByUser(List<Long> userIds) {
    List<UsersCoursesRecord> rows =
        jooq.dsl()
            .selectFrom(USERS_COURSES)
            .where(USERS_COURSES.USER_ID.in(userIds))
            .and(USERS_COURSES.ACTIVE.isTrue())
            .fetch();
    if (rows.isEmpty()) {
      return Map.of();
    }

    Set<Long> courseIds = rows.stream().map(UsersCoursesRecord::getCourseId).collect(toSet());
    Map<Long, Course> courses =
        jooq.dsl()
            .selectFrom(COURSES)
            .where(COURSES.ID.in(courseIds))
            .fetchMap(COURSES.ID, mapper::toCourse);

    return groupByUser(
        rows,
        UsersCoursesRecord::getUserId,
        row -> new UserCourse(courses.get(row.getCourseId()), row.getLastCourseAccess()));
  }

  private Map<Long, List<Role>> findRolesByUser(List<Long> userIds, Long courseId) {
    List<UsersRolesRecord> rows =
        jooq.dsl()
            .selectFrom(USERS_ROLES)
            .where(USERS_ROLES.USER_ID.in(userIds))
            .and(USERS_ROLES.COURSE_ID.eq(courseId))
            .and(USERS_ROLES.ACTIVE.isTrue())
            .fetch();
    if (rows.isEmpty()) {
      return Map.of();
    }

    Set<Long> roleIds = rows.stream().map(UsersRolesRecord::getRoleId).collect(toSet());
    Map<Long, Role> roles =
        jooq.dsl().selectFrom(ROLES).where(ROLES.ID.in(roleIds)).fetchMap(ROLES.ID, mapper::toRole);

    return groupByUser(rows, UsersRolesRecord::getUserId, row -> roles.get(row.getRoleId()));
  }

  private Map<Long, List<Group>> findGroupsByUser(List<Long> userIds, Long courseId) {
    List<UsersGroupsRecord> rows =
        jooq.dsl()
            .selectFrom(USERS_GROUPS)
            .where(USERS_GROUPS.USER_ID.in(userIds))
            .and(USERS_GROUPS.COURSE_ID.eq(courseId))
            .and(USERS_GROUPS.ACTIVE.isTrue())
            .fetch();
    if (rows.isEmpty()) {
      return Map.of();
    }

    Set<Long> groupIds = rows.stream().map(UsersGroupsRecord::getGroupId).collect(toSet());
    Map<Long, Group> groups =
        jooq.dsl()
            .selectFrom(GROUPS)
            .where(GROUPS.ID.in(groupIds))
            .and(GROUPS.COURSE_ID.eq(courseId))
            .fetchMap(GROUPS.ID, mapper::toGroup);

    return groupByUser(rows, UsersGroupsRecord::getUserId, row -> groups.get(row.getGroupId()));
  }

  private static <R, V> Map<Long, List<V>> groupByUser(
      Collection<R> rows, Function<R, Long> userIdOf, Function<R, V> valueOf) {
    return rows.stream().collect(groupingBy(userIdOf, mapping(valueOf, toList())));
  }
}
