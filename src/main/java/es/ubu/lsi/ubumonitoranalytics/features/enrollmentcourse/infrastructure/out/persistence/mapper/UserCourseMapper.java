
package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface UserCourseMapper {


    List<UserCourseEntity> toUserCoursesEntities(List<Enrollment> enrollments, @Context Boolean active);


    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.courseId", source = "course.id")

    @Mapping(target = "active", expression = "java(active)")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "course", source = "course")
    @Mapping(target = "lastCourseAccess",source = "lastCourseAccess")
    UserCourseEntity toUserCourseEntity(Enrollment enrollment, @Context Boolean active);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "userCourses", ignore = true)
    @Mapping(target = "timeModified", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "showGrades", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "enableCompletion", ignore = true)
    CourseEntity toCourseEntity(Course course);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "userGroups", ignore = true)
    @Mapping(target = "userCourses", ignore = true)
    @Mapping(target = "image", ignore = true)
    UserEntity toUserEntity(User user);

    @Mapping(target = "enrollments", source = "userCourseEntities")
    CourseEnrollment toDomain(Integer courseId, List<UserCourseEntity> userCourseEntities);

    Enrollment toEnrollmentDomain(UserCourseEntity courseEnrollment);

    @Mapping(target = "userPicture",  ignore = true)
    @Mapping(target = "roles", source = "userRoles")
    @Mapping(target = "groups", source = "userGroups")
    @Mapping(target = "courses", source = "userCourses")
    User toUserDomain(UserEntity userEntity);


    @Mapping(target = "shortName", ignore = true)
    Course toCourseDomain(CourseEntity courseEntity);

    @Mapping(target = "name", source = "group.name")
    @Mapping(target = "description", source = "group.description")
    @Mapping(target = "id", source = "group.id")
    Group toGroupDomain(UserGroupEntity userGroupEntity);

    @Mapping(target = "name", source = "role.name")
    @Mapping(target = "id", source = "role.id")
    Role toRoleDomain(UserRoleEntity userRoleEntity);


    @Mapping(target = "shortName", source = "course.shortName")
    @Mapping(target = "fullName", source = "course.fullName")
    @Mapping(target = "id", source = "course.id")
    Course toCourseDomain(UserCourseEntity userCourseEntity);
}
