package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterUserCourseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterUserCoursesResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
// MapperUtils provided by GlobalMapperConfig
import org.mapstruct.Mapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleUserCoursesAdapterMapper {


    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "courses", source = "dto.courses")
    UserCourses toDomain(Integer userId, MoodleAdapterUserCoursesResponseDto dto);


    @Mapping(target = "shortName", source = "shortname")
    @Mapping(target = "timeModified", source = "timemodified")
    @Mapping(target = "startDate", source = "startdate")
    @Mapping(target = "showGrades", source = "showgrades")
    @Mapping(target = "endDate", source = "enddate")
    @Mapping(target = "enableCompletion", source = "enablecompletion")
    @Mapping(target = "fullName", source = "fullname")
    Course toDomainCourse(MoodleAdapterUserCourseDto courseDto);





}
