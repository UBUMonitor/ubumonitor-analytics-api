package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.request.GetUsersCoursesRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleUserCoursesAdapterMapper {

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "userid", source = "userId")
  @Mapping(target = "returnusercount", constant = "0")
  GetUsersCoursesRequestApi toRequest(Integer userId);

  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "enrolledCourses", source = "response")
  UserEnrolledCourses toDomain(Integer userId, List<GetUsersCoursesResponseApi> response);

  @Mapping(target = "lastCourseAccess", source = "lastaccess")
  @Mapping(target = "course", source = "getUsersCoursesResponseApi")
  @Mapping(target = "isFavourite", source = "isfavourite")
  Enrollment toDomainEnrolledCourse(GetUsersCoursesResponseApi getUsersCoursesResponseApi);

  @Mapping(target = "shortName", source = "shortname")
  @Mapping(target = "timeModified", source = "timemodified")
  @Mapping(target = "startDate", source = "startdate")
  @Mapping(target = "showGrades", source = "showgrades")
  @Mapping(target = "endDate", source = "enddate")
  @Mapping(target = "enableCompletion", source = "enablecompletion")
  @Mapping(target = "fullName", source = "fullname")
  Course toDomainCourse(GetUsersCoursesResponseApi getUsersCoursesResponseApi);
}
