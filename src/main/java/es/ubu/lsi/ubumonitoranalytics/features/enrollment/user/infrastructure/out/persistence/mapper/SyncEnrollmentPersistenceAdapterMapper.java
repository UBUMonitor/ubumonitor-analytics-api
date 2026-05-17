package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence.mapper;


import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(config = GlobalMapperConfig.class)
public interface SyncEnrollmentPersistenceAdapterMapper {


    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toRecord(Course course, @MappingTarget CoursesRecord coursesRecord);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "active", ignore = true)
    void toRecord(Enrollment enrollment, @MappingTarget UsersCoursesRecord usersCoursesRecord);
}
