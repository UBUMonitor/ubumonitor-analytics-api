package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.*;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps jOOQ records to enrollment domain models. */
@Mapper(config = GlobalMapperConfig.class)
public interface EnrollmentReadMapper {

  /** Profile pictures are served by a dedicated endpoint, so they are not loaded here. */
  @Mapping(target = "userPicture", ignore = true)
  User toUser(UsersRecord source);

  Course toCourse(CoursesRecord source);

  Group toGroup(CourseGroupsRecord source);

  Role toRole(RolesRecord source);
}
