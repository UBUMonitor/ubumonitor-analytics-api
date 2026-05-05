package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.infrastructure.in.rest;


import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.UserEnrollmentInfoDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseEnrollmentsMapper {



    @Mapping(target = "users", source = "enrollments")
    CourseEnrollmentsResponseDto toDto(CourseEnrollment enrollments);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "roles", source = "user.roles")
    @Mapping(target = "groups", source = "user.groups")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "courses", source = "user.courses")
    UserEnrollmentInfoDto toUserEnrollmentInfoDto(Enrollment enrollment);


}
