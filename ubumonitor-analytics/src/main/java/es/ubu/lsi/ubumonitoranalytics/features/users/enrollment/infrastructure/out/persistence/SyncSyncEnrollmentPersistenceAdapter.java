package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;

import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.out.SyncEnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.persistence.mapper.SyncEnrollmentPersistenceAdapterMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersCoursesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/** Stores the current user's course enrollment data in tenant persistence. */
@Component
@RequiredArgsConstructor
public class SyncSyncEnrollmentPersistenceAdapter implements SyncEnrollmentPersistencePort {

  private final Jooq jooq;
  private final SyncEnrollmentPersistenceAdapterMapper mapper;

  @Override
  public void sync(UserEnrolledCourses userEnrolledCourses) {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    upsertUser(userEnrolledCourses.getUserId());
    deactivateActualUsersCourses(userEnrolledCourses.getUserId());

    upsertCourses(userEnrolledCourses, now);
    upsertUserCourses(userEnrolledCourses, now);
    setCourseLoaded(userEnrolledCourses);
  }

  private void upsertUser(Long userId) {

    jooq.dsl().insertInto(USERS).set(USERS.ID, userId).onConflict(USERS.ID).doNothing().execute();
  }

  private void deactivateActualUsersCourses(Long userId) {
    jooq.dsl()
        .update(USERS_COURSES)
        .set(USERS_COURSES.ACTIVE, false)
        .set(USERS_COURSES.UPDATED_AT, DSL.currentOffsetDateTime())
        .where(USERS_COURSES.USER_ID.eq(userId))
        .and(USERS_COURSES.ACTIVE.eq(true))
        .execute();
  }

  private void upsertCourses(UserEnrolledCourses userEnrolledCourses, OffsetDateTime now) {
    List<CoursesRecord> records =
        userEnrolledCourses.getEnrolledCourses().stream()
            .map(Enrollment::getCourse)
            .map(c -> toCoursesRecord(c, now))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private CoursesRecord toCoursesRecord(Course course, OffsetDateTime now) {
    CoursesRecord r = jooq.dsl().newRecord(COURSES);
    mapper.toRecord(course, r);
    r.setUpdatedAt(now);
    return r;
  }

  private void upsertUserCourses(UserEnrolledCourses userEnrolledCourses, OffsetDateTime now) {
    List<UsersCoursesRecord> records =
        userEnrolledCourses.getEnrolledCourses().stream()
            .map(e -> toUserCoursesRecord(userEnrolledCourses.getUserId(), e, now))
            .toList();

    jooq.dsl().batchMerge(records).execute();
  }

  private UsersCoursesRecord toUserCoursesRecord(
      Long userId, Enrollment enrollment, OffsetDateTime now) {
    UsersCoursesRecord r = jooq.dsl().newRecord(USERS_COURSES);
    mapper.toRecord(enrollment, r);
    r.setCourseId(enrollment.getCourse().getId());
    r.setUserId(userId);
    r.setActive(true);
    r.setUpdatedAt(now);

    return r;
  }

  private void setCourseLoaded(UserEnrolledCourses userEnrolledCourses) {
    List<Course> courses =
        userEnrolledCourses.getEnrolledCourses().stream().map(Enrollment::getCourse).toList();

    List<Long> courseIds = courses.stream().map(Course::getId).toList();

    Map<Long, OffsetDateTime> courseLoadedById =
        jooq.dsl()
            .select(COURSES.ID, COURSES.LAST_SYNC)
            .from(COURSES)
            .where(COURSES.ID.in(courseIds))
            .fetchMap(COURSES.ID, COURSES.LAST_SYNC);

    courses.forEach(
        course -> course.setLastSync(courseLoadedById.getOrDefault(course.getId(), null)));
  }
}
