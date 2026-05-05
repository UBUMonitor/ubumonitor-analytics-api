package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseInformationDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.EnrollmentsResponseDto;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface SyncEnrollmentsMapper {

    @Mapping(target = "courses", source = "courses")
    EnrollmentsResponseDto toDto(UserCourses userCourses);



    @Mapping(target = "timeModified", source = "timeModified")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "showGrades", source = "showGrades")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "enableCompletion", source = "enableCompletion")
    @Mapping(target = "fullName", source = "fullName")
    CourseInformationDto toCourseInformationDto(Course course);
}
