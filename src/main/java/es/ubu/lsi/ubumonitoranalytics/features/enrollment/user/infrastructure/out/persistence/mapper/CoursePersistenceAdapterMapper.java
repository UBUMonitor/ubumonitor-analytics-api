package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence.mapper;


import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(config = GlobalMapperConfig.class)
public interface CoursePersistenceAdapterMapper {

    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "userCourses", ignore = true)
    CourseEntity toEntity(Course course);

    List<CourseEntity> toEntity(List<Course> courses);

    Course toDomain(CourseEntity entity);
}
