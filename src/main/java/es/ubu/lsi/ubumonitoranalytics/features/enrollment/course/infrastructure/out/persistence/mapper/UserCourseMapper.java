
package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper;


import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.RolesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersImagesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRolesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(config = GlobalMapperConfig.class)
public interface UserCourseMapper {



    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toUsersRecord(User user, @MappingTarget UsersRecord r);


    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "imageHash", source = "hexHash")
    @Mapping(target = "imageData", source = "data")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toUsersImagesRecord(UserPicture userPicture, @MappingTarget UsersImagesRecord r);


    @Mapping(target = "timeModified", ignore = true)
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "showGrades", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "enableCompletion", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toCoursesRecord(Course course, @MappingTarget CoursesRecord r);

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toGroupsRecord(Group group, @MappingTarget GroupsRecord r);

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toRolesRecord(Role role, @MappingTarget RolesRecord r);


    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toUsersCoursesRecord(UserCourse course, @MappingTarget UsersCoursesRecord r);

    @Mapping(target = "groupId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toUsersGroupsRecord(Group group, @MappingTarget UsersGroupsRecord r);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roleId", ignore = true)
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void toUsersRolesRecord(Role role, @MappingTarget UsersRolesRecord r);


}
