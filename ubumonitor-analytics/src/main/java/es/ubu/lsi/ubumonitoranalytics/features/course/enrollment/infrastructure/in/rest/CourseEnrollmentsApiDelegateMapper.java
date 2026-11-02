package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.*;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.*;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.net.URI;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Maps course enrollments to the REST response. */
@Mapper(config = GlobalMapperConfig.class)
public interface CourseEnrollmentsApiDelegateMapper {

  default CourseEnrollmentsResponseDto toDto(
      CourseEnrollments enrollments, SessionData sessionData) {
    return toDto(enrollments, enrollments.getCourseId(), sessionData);
  }

  @Mapping(target = "users", source = "enrollments")
  @Mapping(target = "courses", source = "source", qualifiedByName = "catalogCourses")
  @Mapping(target = "groups", source = "source", qualifiedByName = "catalogGroups")
  @Mapping(target = "roles", source = "source", qualifiedByName = "catalogRoles")
  CourseEnrollmentsResponseDto toDto(
      CourseEnrollments source, @Context Long courseId, @Context SessionData sessionData);

  @Mapping(target = ".", source = "user")
  @Mapping(target = "imageUrl", source = "user.id", qualifiedByName = "imageUrl")
  @Mapping(target = "lastCourseAccess", source = "enrollment", qualifiedByName = "lastCourseAccess")
  @Mapping(target = "groups", source = "enrollment", qualifiedByName = "groupReferences")
  @Mapping(target = "roles", source = "enrollment", qualifiedByName = "roleReferences")
  UserEnrollmentInfoDto toUserEnrollmentInfoDto(
      Enrollment enrollment, @Context Long courseId, @Context SessionData sessionData);

  @Mapping(target = "id", source = "course.id")
  UserEnrollmentInfoCoursesInnerDto toUserEnrollmentInfoCoursesInnerDto(UserCourse userCourse);

  CourseEnrollmentsResponseCoursesInnerDto toCourseEnrollmentsResponseCoursesInnerDto(
      Course course);

  CourseEnrollmentsResponseGroupsInnerDto toCourseEnrollmentsResponseGroupsInnerDto(Group group);

  CourseEnrollmentsResponseRolesInnerDto toCourseEnrollmentsResponseRolesInnerDto(Role role);

  @Named("catalogCourses")
  default List<CourseEnrollmentsResponseCoursesInnerDto> catalogCourses(CourseEnrollments source) {
    return source.courses().stream().map(this::toCourseEnrollmentsResponseCoursesInnerDto).toList();
  }

  @Named("catalogGroups")
  default List<CourseEnrollmentsResponseGroupsInnerDto> catalogGroups(CourseEnrollments source) {
    return source.groups().stream().map(this::toCourseEnrollmentsResponseGroupsInnerDto).toList();
  }

  @Named("catalogRoles")
  default List<CourseEnrollmentsResponseRolesInnerDto> catalogRoles(CourseEnrollments source) {
    return source.roles().stream().map(this::toCourseEnrollmentsResponseRolesInnerDto).toList();
  }

  @Named("groupReferences")
  default List<UserEnrollmentInfoGroupsInnerDto> groupReferences(Enrollment enrollment) {
    return enrollment.getGroups().stream()
        .map(group -> new UserEnrollmentInfoGroupsInnerDto(group.getId()))
        .toList();
  }

  @Named("roleReferences")
  default List<UserEnrollmentInfoRolesInnerDto> roleReferences(Enrollment enrollment) {
    return enrollment.getRoles().stream()
        .map(role -> new UserEnrollmentInfoRolesInnerDto(role.getId()))
        .toList();
  }

  @Named("lastCourseAccess")
  default Long lastCourseAccess(Enrollment enrollment, @Context Long courseId) {
    return enrollment.lastAccessTo(courseId);
  }

  @Named("imageUrl")
  default URI imageUrl(Long userId, @Context SessionData sessionData) {
    if (userId == null) {
      return null;
    }
    return ServletUriComponentsBuilder.fromCurrentContextPath()
        .path("/api/public/users/{userId}/images/profile")
        .queryParam("username", sessionData.getUsername())
        .queryParam("host", sessionData.getHost())
        .buildAndExpand(userId)
        .toUri();
  }
}
