package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseUsersResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterEnrolledCourseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterEnrolledUserDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterGroupDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterRoleDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseUsersMapper {


    @Mapping(target = "actualCourseId", expression = "java(courseId)")
    @Mapping(target = "users", source = "users")
    UsersResponse toDomain(MoodleAdapterCourseUsersResponseDto moodleAdapterCourseUsersResponseDto, @Context Integer courseId);


    @Mapping(target = "courses", source = "dto", qualifiedByName = "courses")
    @Mapping(target = "firstName", source = "firstname")
    @Mapping(target = "lastName", source = "lastname")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullname")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "groups", source = "groups")
    @Mapping(target = "userPicture.data", ignore = true)
    @Mapping(target = "userPicture.url", source = "dto.privateprofileimageurl")
    @Mapping(target = "lastAccess", source = "lastaccess")
    @Mapping(target = "firstAccess", source = "firstaccess")
    User toUserDomain(MoodleAdapterEnrolledUserDto dto, @Context Integer courseId);

    @Mapping(target = "lastCourseAccess", source = "dto.lastcourseaccess", qualifiedByName = "lastCourseAccess")
    @Mapping(target = "isFavourite", ignore = true)
    @Mapping(target = "course", expression = "java(toCourseDomain(courseDto))")
    UserCourse toUserCourseDomain(MoodleAdapterEnrolledUserDto dto, @Context MoodleAdapterEnrolledCourseDto courseDto, @Context Integer courseId);

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

    @Named("courses")
    default List<UserCourse> courses(MoodleAdapterEnrolledUserDto dto, @Context Integer courseId) {
        if (dto.getEnrolledcourses() == null) {
            return List.of();
        }
        return dto.getEnrolledcourses()
            .stream()
            .map(e -> toUserCourseDomain(dto, e, courseId))
            .toList();
    }

    @Named("lastCourseAccess")
    default OffsetDateTime lastCourseAccess(OffsetDateTime lastCourseAccess, @Context MoodleAdapterEnrolledCourseDto courseDto, @Context Integer courseId) {
        if (courseId.equals(courseDto.getId())) {
            return lastCourseAccess;
        }
        return null;
    }

}

