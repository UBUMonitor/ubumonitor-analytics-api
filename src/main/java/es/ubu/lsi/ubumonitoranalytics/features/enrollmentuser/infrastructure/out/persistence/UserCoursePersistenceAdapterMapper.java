package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import org.mapstruct.Mapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface UserCoursePersistenceAdapterMapper {


    List<CourseEntity> toCourseEntity(List<Course> courses);

    @Mapping(target = "userCourses", ignore = true)
    CourseEntity toCourseEntity(Course course);

    List<Course> toDomain(List<CourseEntity> courseEntities);

    Course toDomain(CourseEntity courseEntity);

}
