package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import org.mapstruct.Mapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(config = GlobalMapperConfig.class)
public interface CourseMapper {
    // to domain

    @Mapping(target = "shortName", ignore = true)
    Course toCourseDomain(CourseEntity courseEntity);



    // to DTO
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "timeModified",  ignore = true)
    @Mapping(target = "startDate",  ignore = true)
    @Mapping(target = "showGrades",  ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "enableCompletion", ignore = true)
    @Mapping(target = "userCourses", ignore = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "id",  ignore = true)
    void updateCourseEntity(Course course, @MappingTarget CourseEntity courseEntity);

    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "timeModified",  ignore = true)
    @Mapping(target = "startDate",  ignore = true)
    @Mapping(target = "showGrades",  ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "enableCompletion", ignore = true)
    @Mapping(target = "userCourses", ignore = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "id",  source = "id")
    CourseEntity createCourseEntity(Course course);

}

