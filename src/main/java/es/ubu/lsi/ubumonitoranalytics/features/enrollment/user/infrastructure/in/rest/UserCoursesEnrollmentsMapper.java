package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseInformationDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.EnrollmentsResponseDto;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface UserCoursesEnrollmentsMapper {

    @Mapping(target = "courses", source = "enrolledCourses")
    EnrollmentsResponseDto toDto(UserEnrolledCourses userEnrolledCourses);


    @Mapping(target = ".", source = "course")
    CourseInformationDto toCourseInformationDto(Enrollment enrollment);

    @Mapping(target = "timeModified", source = "timeModified")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "showGrades", source = "showGrades")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "endDate", source = "endDate")
    @Mapping(target = "enableCompletion", source = "enableCompletion")
    @Mapping(target = "fullName", source = "fullName")
    CourseInformationDto toCourseInformationDto(Course course);
}

