package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.EnrolledcourseApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GetEnrolledUsersResponseApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GroupApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.RoleApi;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.MapperUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Mapper(config = GlobalMapperConfig.class)
public interface EnrolledUsersMapper {


    @Mapping(target = "actualCourseId", source = "courseId")
    @Mapping(target = "users", source = "users")
    UsersResponse toDomain(List<GetEnrolledUsersResponseApi> users, Integer courseId, @Context Integer contextCourseId);


    @Mapping(target = "courses", source = "dto", qualifiedByName = "courses")
    @Mapping(target = "firstName", source = "firstname")
    @Mapping(target = "lastName", source = "lastname")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullname")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "groups", source = "groups")
    @Mapping(target = "userPicture.data", ignore = true)
    @Mapping(target = "userPicture.url", source = "profileimageurl", qualifiedByName = "profileUrl")
    @Mapping(target = "lastAccess", source = "lastaccess")
    @Mapping(target = "firstAccess", source = "firstaccess")
    User toUserDomain(GetEnrolledUsersResponseApi dto, @Context Integer courseId);

    @Mapping(target = "lastCourseAccess", source = "dto.lastcourseaccess", qualifiedByName = "lastCourseAccess")
    @Mapping(target = "isFavourite", ignore = true)
    @Mapping(target = "course", expression = "java(toCourseDomain(enrolledcourseApi))")
    UserCourse toUserCourseDomain(GetEnrolledUsersResponseApi dto, @Context EnrolledcourseApi enrolledcourseApi, @Context Integer courseId);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "fullName", source = "fullname")
    @Mapping(target = "shortName", source = "shortname")
    Course toCourseDomain(EnrolledcourseApi enrolledcourseApi);


    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "groupApi", qualifiedByName = "descriptiontext")
    Group toGroupDomain(GroupApi groupApi);

    @Mapping(target = "id", source = "roleid")
    @Mapping(target = "name", source = "roleApi", qualifiedByName = "extractRoleName")
    Role toRoleDomain(RoleApi roleApi);


    @Named("extractRoleName")
    default String extractRoleName(RoleApi roleApi) {
        if (roleApi == null) {
            return null;
        }
        return StringUtils.defaultIfBlank(roleApi.getName(), roleApi.getShortname());
    }

    @Named("courses")
    default List<UserCourse> courses(GetEnrolledUsersResponseApi getEnrolledUsersResponseApi, @Context Integer courseId) {
        if (getEnrolledUsersResponseApi.getEnrolledcourses() == null) {
            return List.of();
        }
        return getEnrolledUsersResponseApi.getEnrolledcourses()
            .stream()
            .map(e -> toUserCourseDomain(getEnrolledUsersResponseApi, e, courseId))
            .toList();
    }

    @Named("lastCourseAccess")
    default OffsetDateTime lastCourseAccess(Integer lastCourseAccess, @Context EnrolledcourseApi enrolledcourseApi, @Context Integer courseId) {
        if (courseId.equals(enrolledcourseApi.getId())) {
            return MapperUtils.unixToOffsetDateTime(lastCourseAccess);
        }
        return null;
    }


    @Named("descriptiontext")
    default String parseSummary(GroupApi group) {
        return MapperUtils.parseMoodleContent(group.getDescription(),
            Optional.ofNullable(group.getDescriptionformat())
                .map(GroupApi.Descriptionformat::value)
                .orElse(null));
    }

}

