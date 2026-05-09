package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseUsersResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterEnrolledCourseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterEnrolledUserDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterGroupDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterRoleDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseUsersMapper {

    @Mapping(target = "courseId", expression = "java(courseId)")
    @Mapping(target = "enrollments", source = "moodleAdapterCourseUsersResponseDto.users")
    CourseEnrollment toDomain(MoodleAdapterCourseUsersResponseDto moodleAdapterCourseUsersResponseDto, @Context Integer courseId);


    @Mapping(target = "user", source = "dto")
    @Mapping(target = "course.id", expression = "java(courseId)")
    @Mapping(target = "lastCourseAccess", source = "lastcourseaccess")

    Enrollment toDomain(MoodleAdapterEnrolledUserDto dto, @Context Integer courseId);


    @Mapping(target = "firstName", source = "firstname")
    @Mapping(target = "lastName", source = "lastname")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullname")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "courses", source = "enrolledcourses")
    @Mapping(target = "groups", source = "groups")
    @Mapping(target = "userPicture.data", ignore = true)
    @Mapping(target = "userPicture.url", source = "dto.privateprofileimageurl")
    @Mapping(target = "lastAccess", source = "lastaccess")
    @Mapping(target = "firstAccess", source = "firstaccess")
    User toUserDomain(MoodleAdapterEnrolledUserDto dto);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullname")
    @Mapping(target = "shortName", source = "shortname")
    Course toCourseDomain(MoodleAdapterEnrolledCourseDto courseDto);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "descriptiontext")
    Group toGroupDomain(MoodleAdapterGroupDto groupDto);

    @Mapping(target = "id", source = "roleid")
    @Mapping(target = "name", source = "roleDto", qualifiedByName = "extractRoleName")
    Role toRoleDomain(MoodleAdapterRoleDto roleDto);



    @Named("extractRoleName")
    default String extractRoleName(MoodleAdapterRoleDto roleDto) {
        if (roleDto == null) {
            return null;
        }
        return StringUtils.defaultIfBlank(roleDto.getName(), roleDto.getShortname());
    }

}
