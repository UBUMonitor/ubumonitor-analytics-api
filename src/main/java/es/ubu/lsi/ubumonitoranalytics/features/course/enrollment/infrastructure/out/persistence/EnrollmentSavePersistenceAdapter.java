package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.EnrollmentSavePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.CourseMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.GroupMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.RoleMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.UserCourseMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.UserGroupMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.UserMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper.UserRoleMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.GroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.RoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.GroupRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.RoleRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserCourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserGroupRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class EnrollmentSavePersistenceAdapter implements EnrollmentSavePersistencePort {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRolesRepository;
    private final UserGroupRepository userGroupRepository;

    private final CourseMapper courseMapper;
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserCourseMapper userCourseMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserGroupMapper userGroupMapper;


    @Override
    public void saveEnrollments(CourseEnrollment courseEnrollment) {


        syncCourses(courseEnrollment);
        syncGroups(courseEnrollment);
        syncRoles(courseEnrollment);
        syncUsers(courseEnrollment);


        List<Integer> usersIds = courseEnrollment
            .getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .map(User::getId)
            .distinct()
            .toList();

        syncUserCourses(usersIds, courseEnrollment);
        syncUserRoles(usersIds, courseEnrollment);
        syncUserGroups(usersIds, courseEnrollment);
    }

    private void syncUsers(CourseEnrollment courseEnrollment) {
        Set<Integer> actualUserIds = courseEnrollment.getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .map(User::getId).
            collect(Collectors.toSet());
        Map<Integer, UserEntity> userEntityMap = userRepository.findAllById(actualUserIds)
            .stream()
            .collect(Collectors.toMap(UserEntity::getId, u -> u));
        List<UserEntity> userEntities = new ArrayList<>();
        for (Enrollment enrollment : courseEnrollment.getEnrollments()) {
            User user = enrollment.getUser();
            UserEntity userEntity = userEntityMap.getOrDefault(user.getId(), new UserEntity());
            userMapper.toEntity(user, userEntity);
            userEntities.add(userEntity);

        }
        userRepository.saveAll(userEntities);
    }

    private void syncCourses(CourseEnrollment courseEnrollment) {

        Map<Integer, Course> coursesById = courseEnrollment.getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .map(User::getCourses)
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toMap(Course::getId, c -> c));

        List<CourseEntity> existing = courseRepository.findAllById(coursesById.keySet());

        Set<Integer> existingIds = existing.stream()
            .map(CourseEntity::getId)
            .collect(Collectors.toSet());

        // update (dirty checking)
        for (CourseEntity entity : existing) {
            courseMapper.updateCourseEntity(coursesById.get(entity.getId()), entity);
        }

        // create
        List<CourseEntity> toCreate = coursesById.values().stream()
            .filter(c -> !existingIds.contains(c.getId()))
            .map(courseMapper::createCourseEntity)
            .toList();

        courseRepository.saveAll(toCreate);
    }

    private void syncGroups(CourseEnrollment courseEnrollment) {
        Map<Integer, Group> groupsById = courseEnrollment.getEnrollments()
            .stream()
            .flatMap(e -> e.getUser().getGroups().stream())
            .distinct()
            .collect(Collectors.toMap(Group::getId, g -> g));

        List<GroupEntity> existing = groupRepository.findAllById(groupsById.keySet());

        Set<Integer> existingIds = existing.stream()
            .map(GroupEntity::getId)
            .collect(Collectors.toSet());

        // update (dirty checking)
        for (GroupEntity entity : existing) {
            groupMapper.updateGroupEntity(groupsById.get(entity.getId()), entity);
        }

        // create
        List<GroupEntity> toCreate = groupsById.values().stream()
            .filter(g -> !existingIds.contains(g.getId()))
            .map(groupMapper::createGroupEntity)
            .toList();

        groupRepository.saveAll(toCreate);
    }


    private void syncRoles(CourseEnrollment courseEnrollment) {

        Map<Integer, Role> rolesById = courseEnrollment.getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .flatMap(user -> user.getRoles().stream())
            .distinct()
            .collect(Collectors.toMap(Role::getId, r -> r));

        List<RoleEntity> existing = roleRepository.findAllById(rolesById.keySet());

        Set<Integer> existingIds = existing.stream()
            .map(RoleEntity::getId)
            .collect(Collectors.toSet());

        // update (dirty checking)
        for (RoleEntity entity : existing) {
            roleMapper.updateRoleEntity(rolesById.get(entity.getId()), entity);
        }

        // create
        List<RoleEntity> toCreate = rolesById.values().stream()
            .filter(r -> !existingIds.contains(r.getId()))
            .map(roleMapper::createRoleEntity)
            .toList();

        roleRepository.saveAll(toCreate);
    }

    public void syncUserCourses(List<Integer> usersIds, CourseEnrollment courseEnrollment) {

        // 1. Desactivar relaciones actuales
        userCourseRepository.deactivateByUserIds(usersIds);

        // 2. Usar directamente los enrollments que YA contienen lastCourseAccess
        List<Enrollment> enrollments = courseEnrollment.getEnrollments();

        // 3. Mapear a entidades (asegúrate de que el mapper copie lastCourseAccess)
        List<UserCourseEntity> userCourseEntities =
            userCourseMapper.toUserCoursesEntities(enrollments, true);

        for (Enrollment enrollment : enrollments) {
            User user =  enrollment.getUser();
            List<Course> courses = user.getCourses();

            for (Course course : courses) {
                if (course.getId().equals(courseEnrollment.getCourseId())) {
                    continue;
                }
                Enrollment enrollment1 = new Enrollment();
                enrollment1.setCourse(course);
                enrollment1.setUser(user);
                UserCourseEntity userCourseEntity = userCourseMapper.toUserCourseEntity(enrollment1, true);
                userCourseEntities.add(userCourseEntity);
            }


        }

        // 4. Guardar
        userCourseRepository.saveAll(userCourseEntities);
    }

    public void syncUserRoles(List<Integer> userIds, CourseEnrollment courseEnrollment) {
        userRolesRepository.deactivateByUserIds(userIds);

        List<UserRoleEntity> userRoles = courseEnrollment.getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .map(user -> userRoleMapper.toUserRolesEntities(user, user.getRoles(), courseEnrollment.getCourseId(), true))
            .flatMap(List::stream)
            .toList();

        userRolesRepository.saveAll(userRoles);

    }

    public void syncUserGroups(List<Integer> userIds, CourseEnrollment courseEnrollment) {
        userGroupRepository.deactivateByUserIds(userIds);

        List<UserGroupEntity> userGroups = courseEnrollment.getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .map(user -> userGroupMapper.toUserGroupoEntities(user, user.getGroups(), courseEnrollment.getCourseId(), true))
            .flatMap(List::stream)
            .toList();

        userGroupRepository.saveAll(userGroups);

    }

}
