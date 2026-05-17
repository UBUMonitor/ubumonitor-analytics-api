package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper;


import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GroupsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.RolesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.jooq.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;


@Mapper(config = GlobalMapperConfig.class)
public interface EnrollmentCourseEnrollmentsMapper {

    @Mapping(target = "userPicture", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "courses", ignore = true)
    User toUserDomain(UsersRecord r);

    Course toCourseDomain(CoursesRecord r);

    Group toGroupDomain(GroupsRecord into);

    Role toRoleDomain(RolesRecord into);

    default UserCourse toUserCourseDomain(Record r) {

        UserCourse uc = new UserCourse();

        uc.setCourse(
            toCourseDomain(r.into(COURSES))
        );

        uc.setLastCourseAccess(
            r.get(USERS_COURSES.LAST_COURSE_ACCESS)
        );

        return uc;
    }
}
