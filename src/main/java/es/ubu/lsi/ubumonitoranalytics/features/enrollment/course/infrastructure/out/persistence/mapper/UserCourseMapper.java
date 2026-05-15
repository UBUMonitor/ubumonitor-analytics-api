
package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper;


import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Enrollment;



import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = GlobalMapperConfig.class)
public interface UserCourseMapper {

    @Mapping(target = "isFavourite", ignore = true)
    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.courseId", source = "course.id")
    @Mapping(target = "active", expression = "java(active)")
    @Mapping(target = "lastCourseAccess", source = "lastCourseAccess")
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "user", ignore = true)
    UserCourseEntity toEntity(Enrollment enrollment, @Context Boolean active);


}
