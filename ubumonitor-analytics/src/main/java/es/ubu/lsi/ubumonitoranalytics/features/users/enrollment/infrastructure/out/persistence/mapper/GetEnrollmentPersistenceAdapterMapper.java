package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps persisted course and enrollment records to domain models. */
@Mapper(config = GlobalMapperConfig.class)
public interface GetEnrollmentPersistenceAdapterMapper {

  @Mapping(target = "course.timeModified", source = "course.sourceModifiedAt")
  Enrollment toDomain(CoursesRecord course, UsersCoursesRecord usersCoursesRecord);
}
