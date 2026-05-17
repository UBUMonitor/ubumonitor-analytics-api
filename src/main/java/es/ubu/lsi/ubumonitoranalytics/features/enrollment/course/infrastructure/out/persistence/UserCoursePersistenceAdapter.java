package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserCourseMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.RolesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersImagesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRolesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Groups.GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Roles.ROLES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersGroups.USERS_GROUPS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersImages.USERS_IMAGES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersRoles.USERS_ROLES;


@Component
@RequiredArgsConstructor
public class UserCoursePersistenceAdapter implements UserCoursePersistencePort {

    private final Jooq jooq;
    private final UserCourseMapper mapper;

    @Override
    public void sync(UsersResponse usersResponse) {
        LocalDateTime now = LocalDateTime.now();

        upsertUsers(usersResponse, now);
        upsertCourses(usersResponse, now);
        upsertGroups(usersResponse, now);
        upsertRoles(usersResponse, now);

        List<Integer> userIds = usersResponse.getUsers().stream()
            .map(User::getId)
            .toList();
        deactivateActualUsersCourses(userIds, now);
        deactivateActualUsersGroups(userIds, now);
        deactivateActualUsersRoles(userIds, now);


        upsertUsersCourses(usersResponse, now);
        upsertUsersGroups(usersResponse, now);
        upsertUsersRoles(usersResponse, now);
    }


    private void upsertUsers(UsersResponse usersResponse, LocalDateTime now) {
        List<UsersRecord> records = usersResponse.getUsers()
            .stream()
            .map(u -> toUsersRecord(u, now))
            .toList();

        jooq.dsl().batchMerge(records).execute();

        List<UsersImagesRecord> imagesRecords = usersResponse.getUsers()
            .stream()
            .map(u -> toUsersImagesRecord(u, now))
            .toList();

        jooq.dsl().batchMerge(imagesRecords).execute();

    }

    private UsersImagesRecord toUsersImagesRecord(User user, LocalDateTime now) {
        UsersImagesRecord r = jooq.dsl().newRecord(USERS_IMAGES);

        mapper.toUsersImagesRecord(user.getUserPicture(), r);
        r.setUserId(user.getId());
        r.setUpdatedAt(now);
        return r;
    }

    public UsersRecord toUsersRecord(User user, LocalDateTime now) {
        UsersRecord r = jooq.dsl().newRecord(USERS);
        mapper.toUsersRecord(user, r);
        r.setUpdatedAt(now);
        return r;
    }

    private void upsertCourses(UsersResponse usersResponse, LocalDateTime now) {
        List<CoursesRecord> records = usersResponse.getUsers()
            .stream()
            .map(User::getCourses)
            .flatMap(List::stream)
            .distinct()
            .map(c -> toCoursesRecord(c.getCourse(), now))
            .toList();

        jooq.dsl().batchMerge(records).execute();

    }

    private CoursesRecord toCoursesRecord(Course course, LocalDateTime now) {
        CoursesRecord r = jooq.dsl().newRecord(COURSES);
        mapper.toCoursesRecord(course, r);
        r.setUpdatedAt(now);
        return r;
    }

    private void upsertGroups(UsersResponse usersResponse, LocalDateTime now) {

        List<GroupsRecord> records = usersResponse.getUsers()
            .stream()
            .map(User::getGroups)
            .flatMap(List::stream)
            .distinct()
            .map(g -> toGroupsRecord(g, now))
            .toList();

        jooq.dsl().batchMerge(records).execute();
    }

    private GroupsRecord toGroupsRecord(Group group, LocalDateTime now) {
        GroupsRecord r = jooq.dsl().newRecord(GROUPS);
        mapper.toGroupsRecord(group, r);
        r.setUpdatedAt(now);
        return r;
    }

    private void upsertRoles(UsersResponse usersResponse, LocalDateTime now) {

        List<RolesRecord> records = usersResponse.getUsers()
            .stream()
            .map(User::getRoles)
            .flatMap(List::stream)
            .distinct()
            .map(g -> toRolesRecord(g, now))
            .toList();

        jooq.dsl().batchMerge(records).execute();
    }

    private RolesRecord toRolesRecord(Role role, LocalDateTime now) {
        RolesRecord r = jooq.dsl().newRecord(ROLES);
        mapper.toRolesRecord(role, r);
        r.setUpdatedAt(now);
        return r;
    }

    private void deactivateActualUsersCourses(List<Integer> userIds, LocalDateTime now) {
        jooq.dsl().update(USERS_COURSES)
            .set(USERS_COURSES.ACTIVE, false)
            .set(USERS_COURSES.UPDATED_AT, now)
            .where(USERS_COURSES.USER_ID.in(userIds))
            .and(USERS_COURSES.ACTIVE.eq(true))
            .execute();

    }


    private void deactivateActualUsersGroups(List<Integer> userIds, LocalDateTime now) {
        jooq.dsl().update(USERS_GROUPS)
            .set(USERS_GROUPS.ACTIVE, false)
            .set(USERS_GROUPS.UPDATED_AT, now)
            .where(USERS_GROUPS.USER_ID.in(userIds))
            .and(USERS_GROUPS.ACTIVE.eq(true))
            .execute();
    }

    private void deactivateActualUsersRoles(List<Integer> userIds, LocalDateTime now) {
        jooq.dsl().update(USERS_ROLES)
            .set(USERS_ROLES.ACTIVE, false)
            .set(USERS_ROLES.UPDATED_AT, now)
            .where(USERS_ROLES.USER_ID.in(userIds))
            .and(USERS_ROLES.ACTIVE.eq(true))
            .execute();
    }


    private void upsertUsersCourses(UsersResponse usersResponse, LocalDateTime now) {

        List<UsersCoursesRecord> records = usersResponse.getUsers().stream()
            .flatMap(user -> user.getCourses().stream()
                .map(userCourse -> toUsersCoursesRecord(userCourse, user.getId(), now)))
            .toList();

        jooq.dsl().batchMerge(records).execute();
    }

    private UsersCoursesRecord toUsersCoursesRecord(UserCourse userCourse, Integer userId, LocalDateTime now) {
        UsersCoursesRecord r = jooq.dsl().newRecord(USERS_COURSES);

        mapper.toUsersCoursesRecord(userCourse, r);
        r.setUpdatedAt(now);
        r.setActive(true);
        r.setCourseId(userCourse.getCourse().getId());
        r.setUserId(userId);
        return r;
    }

    private void upsertUsersGroups(UsersResponse usersResponse, LocalDateTime now) {

        List<UsersGroupsRecord> records = usersResponse.getUsers().stream()
            .flatMap(user -> user.getGroups().stream()
                .map(group -> toUsersGroupsRecord(group, user.getId(), usersResponse.getActualCourseId(), now)))
            .toList();

        jooq.dsl().batchMerge(records).execute();
    }

    private UsersGroupsRecord toUsersGroupsRecord(Group group, Integer userId, Integer actualCourseId, LocalDateTime now) {
        UsersGroupsRecord r = jooq.dsl().newRecord(USERS_GROUPS);

        mapper.toUsersGroupsRecord(group, r);
        r.setUpdatedAt(now);
        r.setActive(true);
        r.setUserId(userId);
        r.setGroupId(group.getId());
        r.setCourseId(actualCourseId);

        return r;
    }

    private void upsertUsersRoles(UsersResponse usersResponse, LocalDateTime now) {

        List<UsersRolesRecord> records = usersResponse.getUsers().stream()
            .flatMap(user -> user.getRoles().stream()
                .map(role -> toUsersRolesRecord(role, user.getId(), usersResponse.getActualCourseId(), now)))
            .toList();

        jooq.dsl().batchMerge(records).execute();
    }

    private UsersRolesRecord toUsersRolesRecord(Role role, Integer userId, Integer actualCourseId, LocalDateTime now) {
        UsersRolesRecord r = jooq.dsl().newRecord(USERS_ROLES);

        mapper.toUsersRolesRecord(role, r);
        r.setUpdatedAt(now);
        r.setActive(true);
        r.setUserId(userId);
        r.setRoleId(role.getId());
        r.setCourseId(actualCourseId);
        return r;
    }


}
