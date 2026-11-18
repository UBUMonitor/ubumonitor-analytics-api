package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignmentSubmission {
  private final Long submissionId;
  private final Long assignmentId;
  private final Long courseId;
  private final Long userId;
  private final Integer attemptNumber;
  private final Long timeCreated;
  private final Long timeModified;
  private final Long timeStarted;
  private final String status;
  private final Boolean latest;
  private final String gradingStatus;
}
