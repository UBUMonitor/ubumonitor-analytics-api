package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.AssignmentSubmissions.ASSIGNMENT_SUBMISSIONS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.CourseAssignments.COURSE_ASSIGNMENTS;

import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out.CourseAssignmentSubmissionsPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.AssignmentSubmissionsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CourseAssignmentsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CourseAssignmentSubmissionsPersistenceAdapter
    implements CourseAssignmentSubmissionsPersistencePort {

  private final Jooq jooq;
  private final CourseAssignmentSubmissionsPersistenceMapper mapper;

  @Override
  @Transactional
  public void syncAssignments(Long courseId, List<CourseAssignment> assignments) {
    long now = Instant.now().getEpochSecond();
    deactivateAssignments(courseId, now);
    List<CourseAssignmentsRecord> assignmentRecords = mapper.toAssignmentRecords(assignments, now);
    if (!assignmentRecords.isEmpty()) {
      jooq.dsl().batchMerge(assignmentRecords).execute();
    }
  }

  @Override
  @Transactional
  public void syncSubmissions(Long courseId, List<AssignmentSubmission> submissions) {
    long now = Instant.now().getEpochSecond();
    jooq.dsl()
        .update(ASSIGNMENT_SUBMISSIONS)
        .set(ASSIGNMENT_SUBMISSIONS.ACTIVE, false)
        .set(ASSIGNMENT_SUBMISSIONS.UPDATED_AT, now)
        .where(ASSIGNMENT_SUBMISSIONS.COURSE_ID.eq(courseId))
        .and(ASSIGNMENT_SUBMISSIONS.ACTIVE.isTrue())
        .execute();

    List<AssignmentSubmissionsRecord> records = mapper.toRecords(submissions, now);
    if (!records.isEmpty()) {
      jooq.dsl().batchMerge(records).execute();
    }
  }

  private void deactivateAssignments(Long courseId, long now) {
    jooq.dsl()
        .update(COURSE_ASSIGNMENTS)
        .set(COURSE_ASSIGNMENTS.ACTIVE, false)
        .set(COURSE_ASSIGNMENTS.UPDATED_AT, now)
        .where(COURSE_ASSIGNMENTS.COURSE_ID.eq(courseId))
        .and(COURSE_ASSIGNMENTS.ACTIVE.isTrue())
        .execute();
  }

  @Override
  public List<CourseAssignment> getAssignments(Long courseId) {
    return jooq.dsl()
        .selectFrom(COURSE_ASSIGNMENTS)
        .where(COURSE_ASSIGNMENTS.COURSE_ID.eq(courseId))
        .and(COURSE_ASSIGNMENTS.ACTIVE.isTrue())
        .orderBy(COURSE_ASSIGNMENTS.MODULE_ID)
        .fetch(mapper::toAssignmentDomain);
  }

  @Override
  public List<AssignmentSubmission> get(
      Long courseId, List<Long> assignmentIds, List<Long> userIds) {
    if (assignmentIds.isEmpty() || userIds.isEmpty()) {
      return List.of();
    }
    return jooq.dsl()
        .selectFrom(ASSIGNMENT_SUBMISSIONS)
        .where(ASSIGNMENT_SUBMISSIONS.COURSE_ID.eq(courseId))
        .and(ASSIGNMENT_SUBMISSIONS.ACTIVE.isTrue())
        .and(ASSIGNMENT_SUBMISSIONS.ASSIGNMENT_ID.in(assignmentIds))
        .and(ASSIGNMENT_SUBMISSIONS.USER_ID.in(userIds))
        .orderBy(
            ASSIGNMENT_SUBMISSIONS.ASSIGNMENT_ID,
            ASSIGNMENT_SUBMISSIONS.USER_ID,
            ASSIGNMENT_SUBMISSIONS.ATTEMPT_NUMBER)
        .fetch(mapper::toDomain);
  }
}
