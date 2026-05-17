package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.SyncEnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence.mapper.SyncEnrollmentPersistenceAdapterMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;


@Component
@RequiredArgsConstructor
public class SyncSyncEnrollmentPersistenceAdapter implements SyncEnrollmentPersistencePort {

    private final Jooq jooq;
    private final SyncEnrollmentPersistenceAdapterMapper mapper;


    @Override
    public void sync(UserEnrolledCourses userEnrolledCourses) {
        LocalDateTime now = LocalDateTime.now();
        upsertUser(userEnrolledCourses.getUserId());
        deactivateActualUsersCourses(userEnrolledCourses.getUserId());

        upsertCourses(userEnrolledCourses, now);
        upsertUserCourses(userEnrolledCourses, now);
    }

    private void upsertUser(Integer userId) {

        jooq.dsl().insertInto(USERS)
            .set(USERS.ID, userId)
            .onConflict(USERS.ID)
            .doNothing()
            .execute();
    }

    private void deactivateActualUsersCourses(Integer userId) {
        jooq.dsl().update(USERS_COURSES)
            .set(USERS_COURSES.ACTIVE, false)
            .set(USERS_COURSES.UPDATED_AT, DSL.currentLocalDateTime())
            .where(USERS_COURSES.USER_ID.eq(userId))
            .and(USERS_COURSES.ACTIVE.eq(true))
            .execute();

    }
    private void upsertCourses(UserEnrolledCourses userEnrolledCourses, LocalDateTime now) {
        List<CoursesRecord> records = userEnrolledCourses.getEnrolledCourses()
            .stream()
            .map(Enrollment::getCourse)
            .map(c-> toCoursesRecord(c, now))
            .toList();

        jooq.dsl().batchMerge(records).execute();

    }

    private CoursesRecord toCoursesRecord(Course course, LocalDateTime now) {
        CoursesRecord r = jooq.dsl().newRecord(COURSES);
        mapper.toRecord(course, r);
        r.setUpdatedAt(now);
        return r;
    }

    private void upsertUserCourses(UserEnrolledCourses userEnrolledCourses, LocalDateTime now) {
        List<UsersCoursesRecord> records = userEnrolledCourses.getEnrolledCourses()
            .stream()
            .map(e -> toUserCoursesRecord(userEnrolledCourses.getUserId(), e, now))
            .toList();

        jooq.dsl().batchMerge(records).execute();

    }

    private UsersCoursesRecord toUserCoursesRecord(Integer userId, Enrollment enrollment, LocalDateTime now) {
        UsersCoursesRecord r = jooq.dsl().newRecord(USERS_COURSES);
        mapper.toRecord(enrollment, r);
        r.setCourseId(enrollment.getCourse().getId());
        r.setUserId(userId);
        r.setActive(true);
        r.setUpdatedAt(now);

        return r;
    }
}
