package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.CoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.EnrollmentFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.GroupPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.RolePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserCoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserGroupPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserRolePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncCourseEnrollmentsService implements SyncCourseEnrollmentsUseCase {

    private final EnrollmentFetchPort enrollmentFetchPort;
    private final CurrentSessionContext currentSessionContext;

    private final UserPersistencePort userPersistencePort;
    private final CoursePersistencePort coursePersistencePort;
    private final GroupPersistencePort groupPersistencePort;
    private final RolePersistencePort rolePersistencePort;

    private final UserCoursePersistencePort userCoursePersistencePort;
    private final UserRolePersistencePort userRolePersistencePort;
    private final UserGroupPersistencePort userGroupPersistencePort;

    @Override
    @Transactional
    public CourseEnrollment syncCourseEnrollments(Integer courseId) {
        CourseEnrollment ce = enrollmentFetchPort.fetchCourseEnrolledUsers(
            courseId,
            currentSessionContext.getSessionData().getMoodleToken()
        );

        List<User> users = ce.getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .distinct()
            .toList();

        List<Course> courses = users.stream()
            .flatMap(user -> user.getCourses().stream())
            .distinct()
            .toList();

        List<Group> groups = users.stream()
            .flatMap(user -> user.getGroups().stream())
            .distinct()
            .toList();

        List<Role> roles = users.stream()
            .flatMap(user -> user.getRoles().stream())
            .distinct()
            .toList();

        coursePersistencePort.syncCourses(courses);

        groupPersistencePort.syncGroups(groups);

        rolePersistencePort.syncRoles(roles);

        userPersistencePort.syncUsers(users);


        List<Integer> userIds = users.stream()
            .map(User::getId)
            .toList();

        userCoursePersistencePort.syncUserCourses(
            userIds,
            ce.getEnrollments(),
            courseId
        );

        userRolePersistencePort.syncUserRoles(
            userIds,
            users,
            courseId
        );

        userGroupPersistencePort.syncUserGroups(
            userIds,
            users,
            courseId
        );

        return ce;
    }



}

