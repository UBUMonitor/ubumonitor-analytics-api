package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.EnrolledcourseApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GetEnrolledUsersResponseApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GroupApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.RoleApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.*;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.MapperUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Maps Moodle enrolled-user responses to the enrollment domain model. */
@Mapper(config = GlobalMapperConfig.class)
public interface EnrolledUsersMapper {

  /**
   * @param courseId Moodle course the users are enrolled in
   * @param users response of Moodle's {@code core_enrol_get_enrolled_users}
   * @return the enrollments of the course
   */
  default CourseEnrollments toDomain(Long courseId, List<GetEnrolledUsersResponseApi> users) {
    if (users == null) {
      return CourseEnrollments.empty(courseId);
    }
    return new CourseEnrollments(
        courseId,
        users.stream().filter(Objects::nonNull).map(user -> toEnrollment(user, courseId)).toList());
  }

  @Mapping(target = "user", source = "dto")
  @Mapping(target = "roles", source = "roles")
  @Mapping(target = "groups", source = "groups")
  @Mapping(target = "courses", source = "dto", qualifiedByName = "userCourses")
  Enrollment toEnrollment(GetEnrolledUsersResponseApi dto, @Context Long courseId);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "fullName", source = "fullname")
  @Mapping(target = "firstName", source = "firstname")
  @Mapping(target = "lastName", source = "lastname")
  @Mapping(target = "email", source = "email")
  @Mapping(target = "firstAccess", source = "firstaccess", qualifiedByName = "nullableTimestamp")
  @Mapping(target = "lastAccess", source = "lastaccess", qualifiedByName = "nullableTimestamp")
  @Mapping(target = "userPicture", source = "dto")
  User toUser(GetEnrolledUsersResponseApi dto);

  @Mapping(target = "url", source = "profileimageurl", qualifiedByName = "profileUrl")
  @Mapping(target = "data", ignore = true)
  @Mapping(target = "contentType", ignore = true)
  @Mapping(target = "hexHash", ignore = true)
  UserPicture toUserPicture(GetEnrolledUsersResponseApi dto);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "fullName", source = "fullname")
  @Mapping(target = "shortName", source = "shortname")
  @Mapping(target = "lastSync", ignore = true)
  Course toCourse(EnrolledcourseApi enrolledcourse);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "groupApi", qualifiedByName = "descriptionText")
  Group toGroup(GroupApi groupApi);

  @Mapping(target = "id", source = "roleid")
  @Mapping(target = "name", source = "roleApi", qualifiedByName = "roleName")
  Role toRole(RoleApi roleApi);

  @Named("userCourses")
  default List<UserCourse> userCourses(GetEnrolledUsersResponseApi dto, @Context Long courseId) {
    if (dto.getEnrolledcourses() == null) {
      return List.of();
    }
    return dto.getEnrolledcourses().stream()
        .map(enrolledcourse -> toUserCourse(dto, enrolledcourse, courseId))
        .toList();
  }

  /**
   * Moodle only reports the last course access for the course being queried, so every other course
   * carries a {@code null} access instead of a misleading value.
   */
  @Mapping(target = "course", expression = "java(toCourse(enrolledcourse))")
  @Mapping(
      target = "lastCourseAccess",
      source = "dto.lastcourseaccess",
      qualifiedByName = "lastCourseAccess")
  UserCourse toUserCourse(
      GetEnrolledUsersResponseApi dto,
      @Context EnrolledcourseApi enrolledcourse,
      @Context Long courseId);

  @Named("lastCourseAccess")
  default Long lastCourseAccess(
      Long lastCourseAccess, @Context EnrolledcourseApi enrolledcourse, @Context Long courseId) {
    return courseId.equals(enrolledcourse.getId())
        ? MapperUtils.nullableTimestamp(lastCourseAccess)
        : null;
  }

  @Named("roleName")
  default String roleName(RoleApi roleApi) {
    if (roleApi == null) {
      return null;
    }
    return StringUtils.defaultIfBlank(roleApi.getName(), roleApi.getShortname());
  }

  @Named("descriptionText")
  default String descriptionText(GroupApi group) {
    return MapperUtils.parseMoodleContent(
        group.getDescription(),
        Optional.ofNullable(group.getDescriptionformat())
            .map(GroupApi.Descriptionformat::value)
            .orElse(null));
  }
}
