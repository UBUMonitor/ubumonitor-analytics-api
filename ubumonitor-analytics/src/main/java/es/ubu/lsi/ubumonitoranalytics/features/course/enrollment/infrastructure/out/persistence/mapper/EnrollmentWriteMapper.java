package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.RolesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersImagesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Copies enrollment domain models onto jOOQ records. Link tables (users_courses, users_groups,
 * users_roles) carry no domain data beyond their keys and are filled directly by the adapter.
 */
@Mapper(config = GlobalMapperConfig.class)
public interface EnrollmentWriteMapper {

  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void toUsersRecord(User user, @MappingTarget UsersRecord target);

  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "imageHash", source = "hexHash")
  @Mapping(target = "imageData", source = "data")
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void toUsersImagesRecord(UserPicture picture, @MappingTarget UsersImagesRecord target);

  @Mapping(target = "lastSync", ignore = true)
  @Mapping(target = "timeModified", ignore = true)
  @Mapping(target = "startDate", ignore = true)
  @Mapping(target = "showGrades", ignore = true)
  @Mapping(target = "endDate", ignore = true)
  @Mapping(target = "enableCompletion", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void toCoursesRecord(Course course, @MappingTarget CoursesRecord target);

  @Mapping(target = "courseId", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void toGroupsRecord(Group group, @MappingTarget GroupsRecord target);

  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void toRolesRecord(Role role, @MappingTarget RolesRecord target);
}
