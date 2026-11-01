package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersActivities.USERS_ACTIVITIES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.GetCourseActivityCompletionPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersActivitiesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

/** Reads course sections and modules from tenant persistence. */
@Component
@RequiredArgsConstructor
public class GetCourseActivityCompletionPersistenceAdapter
    implements GetCourseActivityCompletionPersistencePort {

  private final CourseActivityCompletionPersistenceMapper mapper;
  private final Jooq jooq;

  @Override
  public ActivityCompletionResult getCourseActivitiesCompletions(Long courseId) {
    return fetch(courseId, DSL.noCondition());
  }

  @Override
  public ActivityCompletionResult getCourseActivitiesCompletions(
      Long courseId, List<Long> modulesIds, List<Long> userIds) {
    return fetch(
        courseId,
        USERS_ACTIVITIES.MODULE_ID.in(modulesIds).and(USERS_ACTIVITIES.USER_ID.in(userIds)));
  }

  private ActivityCompletionResult fetch(Long courseId, Condition filter) {
    List<UsersActivitiesRecord> records =
        jooq.dsl()
            .select(USERS_ACTIVITIES.fields())
            .from(USERS_ACTIVITIES)
            .join(USERS_COURSES)
            .on(
                USERS_COURSES
                    .USER_ID
                    .eq(USERS_ACTIVITIES.USER_ID)
                    .and(USERS_COURSES.COURSE_ID.eq(USERS_ACTIVITIES.COURSE_ID)))
            .where(USERS_ACTIVITIES.COURSE_ID.eq(courseId))
            .and(USERS_COURSES.ACTIVE.isTrue())
            .and(filter)
            .fetchInto(USERS_ACTIVITIES);

    return mapper.toDomain(records, courseId);
  }
}
