package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.PersistenceFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence.mapper.GetEnrollmentPersistenceAdapterMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sites.SITES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;


@Component
@RequiredArgsConstructor
public class PersistenceFetchAdapter implements PersistenceFetchPort {

    private final Jooq jooq;
    private final GetEnrollmentPersistenceAdapterMapper mapper;

    @Override
    public Integer getUserIdByUserName(String userName) {
        return jooq.dsl()
            .select(SITES.USER_ID)
            .from(SITES)
            .where(SITES.USER_NAME.eq(userName))
            .fetchOneInto(Integer.class);
    }

    @Override
    public UserEnrolledCourses getEnrolledCourses(Integer userId) {
         Map<Integer, UsersCoursesRecord> byCourseId =  jooq.dsl().selectFrom(USERS_COURSES)
                .where(USERS_COURSES.USER_ID.eq(userId))
                .fetch()
                .intoMap(USERS_COURSES.COURSE_ID);

         List<CoursesRecord> coursesRecords = jooq.dsl().selectFrom(COURSES)
             .where(COURSES.ID.in(byCourseId.keySet()))
             .fetch();

         List<Enrollment> enrollments = coursesRecords.stream()
                 .map(c-> mapper.toDomain(c, byCourseId.get(c.getId())))
                 .toList();

         return UserEnrolledCourses.builder()
             .userId(userId)
             .enrolledCourses(enrollments)
             .build();

    }
}
