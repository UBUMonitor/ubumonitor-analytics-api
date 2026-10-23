package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.request.GetUsersCoursesRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps Moodle user-course responses to application enrollment models. */
@Mapper(config = GlobalMapperConfig.class)
public interface MoodleUserCoursesAdapterMapper {

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.userid", source = "userId")
  @Mapping(target = "args.returnusercount", constant = "0")
  GetUsersCoursesRequestApi toRequest(Long userId);

  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "enrolledCourses", source = "response")
  UserEnrolledCourses toDomain(Long userId, List<GetUsersCoursesResponseApi> response);

  @Mapping(target = "lastCourseAccess", source = "lastaccess")
  @Mapping(target = "course", source = "getUsersCoursesResponseApi")
  @Mapping(target = "isFavourite", source = "isfavourite")
  Enrollment toDomainEnrolledCourse(GetUsersCoursesResponseApi getUsersCoursesResponseApi);

  @Mapping(target = "hasBeenLoaded", ignore = true)
  @Mapping(target = "shortName", source = "shortname")
  @Mapping(target = "timeModified", source = "timemodified")
  @Mapping(target = "startDate", source = "startdate")
  @Mapping(target = "showGrades", source = "showgrades")
  @Mapping(target = "endDate", source = "enddate")
  @Mapping(target = "enableCompletion", source = "enablecompletion")
  @Mapping(target = "fullName", source = "fullname")
  Course toDomainCourse(GetUsersCoursesResponseApi getUsersCoursesResponseApi);
}
