package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterUserCourseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterUserCoursesResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
// MapperUtils provided by GlobalMapperConfig
import org.mapstruct.Mapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleUserCoursesAdapterMapper {


    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "enrolledCourses", source = "dto.courses")
    UserEnrolledCourses toDomain(Integer userId, MoodleAdapterUserCoursesResponseDto dto);

    @Mapping(target = "lastCourseAccess", source = "lastaccess")
    @Mapping(target = "course", source = "moodleAdapterUserCourseDto")
    @Mapping(target = "isFavourite", source = "isfavourite")
    Enrollment toDomainEnrolledCourse(MoodleAdapterUserCourseDto moodleAdapterUserCourseDto);

    @Mapping(target = "shortName", source = "shortname")
    @Mapping(target = "timeModified", source = "timemodified")
    @Mapping(target = "startDate", source = "startdate")
    @Mapping(target = "showGrades", source = "showgrades")
    @Mapping(target = "endDate", source = "enddate")
    @Mapping(target = "enableCompletion", source = "enablecompletion")
    @Mapping(target = "fullName", source = "fullname")
    Course toDomainCourse(MoodleAdapterUserCourseDto courseDto);





}

