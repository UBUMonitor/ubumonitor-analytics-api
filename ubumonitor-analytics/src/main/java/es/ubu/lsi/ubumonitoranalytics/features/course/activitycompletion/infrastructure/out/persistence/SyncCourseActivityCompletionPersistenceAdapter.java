package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersActivities.USERS_ACTIVITIES;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.CourseActivityCompletionPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersActivitiesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

/** Stores synchronized course sections and modules in tenant persistence. */
@Component
@RequiredArgsConstructor
public class SyncCourseActivityCompletionPersistenceAdapter
    implements CourseActivityCompletionPersistencePort {

  private final Jooq jooq;
  private final CourseActivityCompletionPersistenceMapper mapper;

  @Override
  public void save(ActivityCompletionResult result) {
    DSLContext dsl = jooq.dsl();

    dsl.update(USERS_ACTIVITIES)
        .set(USERS_ACTIVITIES.ACTIVE, false)
        .where(USERS_ACTIVITIES.COURSE_ID.eq(result.getCourseId()))
        .and(USERS_ACTIVITIES.ACTIVE.isTrue())
        .execute();

    List<UsersActivitiesRecord> records =
        mapper.toRecords(result.getActivityCompletions(), Instant.now().getEpochSecond());
    for (UsersActivitiesRecord record : records) {
      record.setCourseId(result.getCourseId());
    }

    dsl.batchMerge(records).execute();
  }
}
