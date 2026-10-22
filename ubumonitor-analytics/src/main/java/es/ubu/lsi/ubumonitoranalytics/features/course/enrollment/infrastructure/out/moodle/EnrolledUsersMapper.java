package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.EnrolledcourseApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GetEnrolledUsersResponseApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GroupApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.RoleApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.*;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.MapperUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Maps Moodle enrolled-user responses to application enrollment models. */
@Mapper(config = GlobalMapperConfig.class)
public interface EnrolledUsersMapper {

  @Mapping(target = "actualCourseId", source = "courseId")
  @Mapping(target = "users", source = "users")
  UsersResponse toDomain(
      List<GetEnrolledUsersResponseApi> users, Long courseId, @Context Long contextCourseId);

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
  User toUserDomain(GetEnrolledUsersResponseApi dto, @Context Long courseId);

  @Mapping(
      target = "lastCourseAccess",
      source = "dto.lastcourseaccess",
      qualifiedByName = "lastCourseAccess")
  @Mapping(target = "isFavourite", ignore = true)
  @Mapping(target = "course", expression = "java(toCourseDomain(enrolledcourseApi))")
  UserCourse toUserCourseDomain(
      GetEnrolledUsersResponseApi dto,
      @Context EnrolledcourseApi enrolledcourseApi,
      @Context Long courseId);

  @Mapping(target = "hasBeenLoaded", ignore = true)
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
  default List<UserCourse> courses(
      GetEnrolledUsersResponseApi getEnrolledUsersResponseApi, @Context Long courseId) {
    if (getEnrolledUsersResponseApi.getEnrolledcourses() == null) {
      return List.of();
    }
    return getEnrolledUsersResponseApi.getEnrolledcourses().stream()
        .map(e -> toUserCourseDomain(getEnrolledUsersResponseApi, e, courseId))
        .toList();
  }

  @Named("lastCourseAccess")
  default LocalDateTime lastCourseAccess(
      Long lastCourseAccess, @Context EnrolledcourseApi enrolledcourseApi, @Context Long courseId) {
    if (courseId.equals(enrolledcourseApi.getId())) {
      return MapperUtils.toLocalDateTime(lastCourseAccess);
    }
    return null;
  }

  @Named("descriptiontext")
  default String parseSummary(GroupApi group) {
    return MapperUtils.parseMoodleContent(
        group.getDescription(),
        Optional.ofNullable(group.getDescriptionformat())
            .map(GroupApi.Descriptionformat::value)
            .orElse(null));
  }
}
