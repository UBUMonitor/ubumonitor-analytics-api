package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
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

    default List<UserCourseEntity> toUserCourseEntities(UserCourses userCourses, Boolean active) {
        return userCourses.getCourses().stream()
            .map(course -> toUserCourseEntity(userCourses.getUserId(), course, active))
            .toList();
    }

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "lastCourseAccess", ignore = true)
    @Mapping(target = "active", source = "active")
    @Mapping(target = "id.courseId", source = "course.id")
    @Mapping(target = "id.userId", source = "userId")
    UserCourseEntity toUserCourseEntity(Integer userId, Course course, Boolean active);
}
