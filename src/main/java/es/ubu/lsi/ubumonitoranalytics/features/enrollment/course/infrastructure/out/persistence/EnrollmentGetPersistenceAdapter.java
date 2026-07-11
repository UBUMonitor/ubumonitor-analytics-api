package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Groups.GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Roles.ROLES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGroups.USERS_GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersRoles.USERS_ROLES;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.EnrollmentGetPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.EnrollmentCourseEnrollmentsMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentGetPersistenceAdapter implements EnrollmentGetPersistencePort {
  private final Jooq jooq;
  private final EnrollmentCourseEnrollmentsMapper mapper;

  @Override
  public UsersResponse getCourseEnrollment(Integer courseId) {
    Map<Integer, User> users = fetchUsers(courseId);

    List<Integer> userIds = new ArrayList<>(users.keySet());

    Map<Integer, List<UserCourse>> userCourses = fetchCourses(userIds);
    Map<Integer, List<Group>> userGroups = fetchGroups(userIds);
    Map<Integer, List<Role>> userRoles = fetchRoles(userIds);

    // ensamblado final
    users
        .values()
        .forEach(
            u -> {
              u.setCourses(userCourses.getOrDefault(u.getId(), List.of()));
              u.setGroups(userGroups.getOrDefault(u.getId(), List.of()));
              u.setRoles(userRoles.getOrDefault(u.getId(), List.of()));
            });

    return UsersResponse.builder()
        .users(new ArrayList<>(users.values()))
        .actualCourseId(courseId)
        .build();
  }

  private Map<Integer, User> fetchUsers(Integer courseId) {

    return jooq.dsl()
        .select(USERS.fields())
        .from(USERS)
        .join(USERS_COURSES)
        .on(USERS.ID.eq(USERS_COURSES.USER_ID))
        .where(USERS_COURSES.COURSE_ID.eq(courseId))
        .and(USERS_COURSES.ACTIVE.isTrue())
        .fetchMap(USERS.ID, r -> mapper.toUserDomain(r.into(USERS)));
  }

  private Map<Integer, List<UserCourse>> fetchCourses(List<Integer> userIds) {

    return jooq.dsl()
        .select(
            USERS_COURSES.USER_ID,
            USERS_COURSES.LAST_COURSE_ACCESS,
            COURSES.ID,
            COURSES.FULL_NAME,
            COURSES.SHORT_NAME)
        .from(USERS_COURSES)
        .join(COURSES)
        .on(COURSES.ID.eq(USERS_COURSES.COURSE_ID))
        .where(USERS_COURSES.USER_ID.in(userIds))
        .and(USERS_COURSES.ACTIVE.isTrue())
        .fetchGroups(r -> r.get(USERS_COURSES.USER_ID), mapper::toUserCourseDomain);
  }

  private Map<Integer, List<Group>> fetchGroups(List<Integer> userIds) {

    return jooq.dsl()
        .select(USERS_GROUPS.USER_ID, GROUPS.ID, GROUPS.NAME)
        .from(USERS_GROUPS)
        .join(GROUPS)
        .on(GROUPS.ID.eq(USERS_GROUPS.GROUP_ID))
        .where(USERS_GROUPS.USER_ID.in(userIds))
        .and(USERS_GROUPS.ACTIVE.isTrue())
        .fetchGroups(r -> r.get(USERS_GROUPS.USER_ID), r -> mapper.toGroupDomain(r.into(GROUPS)));
  }

  private Map<Integer, List<Role>> fetchRoles(List<Integer> userIds) {

    return jooq.dsl()
        .select(USERS_ROLES.USER_ID, ROLES.ID, ROLES.NAME)
        .from(USERS_ROLES)
        .join(ROLES)
        .on(ROLES.ID.eq(USERS_ROLES.ROLE_ID))
        .where(USERS_ROLES.USER_ID.in(userIds))
        .and(USERS_ROLES.ACTIVE.isTrue())
        .fetchGroups(r -> r.get(USERS_ROLES.USER_ID), r -> mapper.toRoleDomain(r.into(ROLES)));
  }
}
