package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface EnrollmentCourseEnrollmentsMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "course", source = "course")
    Enrollment toDomain(UserCourseEntity entity);

    @Mapping(target = "userPicture", ignore = true)
    @Mapping(target = "roles", source = "userRoles")
    @Mapping(target = "groups", source = "userGroups")
    @Mapping(target = "courses", source = "userCourses")
    User toDomain(UserEntity course);

    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "description", source = "group.name")
    @Mapping(target = "id", source = "group.id")
    Group toDomain(UserGroupEntity userGroupEntity);

    @Mapping(target = "name", source = "role.name")
    @Mapping(target = "id", source = "role.id")
    Role toDomain(UserRoleEntity userRoleEntity);


    @Mapping(target = "shortName",  source = "course.shortName")
    @Mapping(target = "fullName", source = "course.fullName")
    @Mapping(target = "id", source = "course.id")
    Course toCourseDomain(UserCourseEntity userCourseEntity);

    @Mapping(target = "enrollments", source = "userCourseEntities")
    CourseEnrollment toDomain(Integer courseId, List<UserCourseEntity> userCourseEntities);
}
