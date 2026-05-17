package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.in.rest;


import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.UserEnrollmentInfoCoursesInnerDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.UserEnrollmentInfoDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseEnrollmentsApiDelegateMapper {



    CourseEnrollmentsResponseDto toDto(UsersResponse usersResponse, @Context Integer courseId);

    @Mapping(target = "lastCourseAccess", source = "user.courses", qualifiedByName = "lastCourseAccess")
    UserEnrollmentInfoDto toUserEnrollmentInfoDto(User user, @Context Integer courseId);

    @Named("lastCourseAccess")
    default OffsetDateTime mapLastCourseAccess(List<UserCourse> userCourses, @Context Integer courseId) {
        return userCourses.stream()
                .filter(uc ->courseId.equals(uc.getCourse().getId()))
                .findFirst()
                .map(UserCourse::getLastCourseAccess)
                .orElse(null);
    }

    @Mapping(target = ".", source = "course")
    UserEnrollmentInfoCoursesInnerDto toUserEnrollmentInfoCoursesInnerDto(UserCourse userCourse);
}

