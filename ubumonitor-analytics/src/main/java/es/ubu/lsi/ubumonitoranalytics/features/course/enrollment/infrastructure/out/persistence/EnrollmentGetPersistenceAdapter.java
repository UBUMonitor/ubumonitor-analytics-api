package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Groups.GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Roles.ROLES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGroups.USERS_GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersRoles.USERS_ROLES;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.EnrollmentGetPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.*;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.EnrollmentCourseEnrollmentsMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Reads course enrollment data from tenant persistence. */
@Component
@RequiredArgsConstructor
public class EnrollmentGetPersistenceAdapter implements EnrollmentGetPersistencePort {
  private final Jooq jooq;
  private final EnrollmentCourseEnrollmentsMapper mapper;

  @Override
  public UsersResponse getCourseEnrollment(Long courseId) {
    Map<Long, User> users = fetchUsers(courseId);

    List<Long> userIds = new ArrayList<>(users.keySet());

    Map<Long, List<UserCourse>> userCourses = fetchCourses(userIds);
    Map<Long, List<Group>> userGroups = fetchGroups(userIds);
    Map<Long, List<Role>> userRoles = fetchRoles(userIds);

    // Assemble the final response.
    for (User user : users.values()) {
      user.setCourses(userCourses.getOrDefault(user.getId(), List.of()));
      user.setGroups(userGroups.getOrDefault(user.getId(), List.of()));
      user.setRoles(userRoles.getOrDefault(user.getId(), List.of()));
    }

    return UsersResponse.builder()
        .users(new ArrayList<>(users.values()))
        .actualCourseId(courseId)
        .build();
  }

  private Map<Long, User> fetchUsers(Long courseId) {

    return jooq.dsl()
        .select(USERS.fields())
        .from(USERS)
        .join(USERS_COURSES)
        .on(USERS.ID.eq(USERS_COURSES.USER_ID))
        .where(USERS_COURSES.COURSE_ID.eq(courseId))
        .and(USERS_COURSES.ACTIVE.isTrue())
        .fetchMap(USERS.ID, r -> mapper.toUserDomain(r.into(USERS)));
  }

  private Map<Long, List<UserCourse>> fetchCourses(List<Long> userIds) {

    return jooq.dsl()
        .select(
            USERS_COURSES.USER_ID,
            USERS_COURSES.LAST_COURSE_ACCESS,
            COURSES.ID,
            COURSES.FULL_NAME,
            COURSES.SHORT_NAME,
            COURSES.HAS_BEEN_LOADED)
        .from(USERS_COURSES)
        .join(COURSES)
        .on(COURSES.ID.eq(USERS_COURSES.COURSE_ID))
        .where(USERS_COURSES.USER_ID.in(userIds))
        .and(USERS_COURSES.ACTIVE.isTrue())
        .fetchGroups(r -> r.get(USERS_COURSES.USER_ID), mapper::toUserCourseDomain);
  }

  private Map<Long, List<Group>> fetchGroups(List<Long> userIds) {

    return jooq.dsl()
        .select(USERS_GROUPS.USER_ID, GROUPS.ID, GROUPS.NAME)
        .from(USERS_GROUPS)
        .join(GROUPS)
        .on(GROUPS.ID.eq(USERS_GROUPS.GROUP_ID))
        .where(USERS_GROUPS.USER_ID.in(userIds))
        .and(USERS_GROUPS.ACTIVE.isTrue())
        .fetchGroups(r -> r.get(USERS_GROUPS.USER_ID), r -> mapper.toGroupDomain(r.into(GROUPS)));
  }

  private Map<Long, List<Role>> fetchRoles(List<Long> userIds) {

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
