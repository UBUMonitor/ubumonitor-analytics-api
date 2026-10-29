package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model;

import lombok.Data;

@Data
public class ActivityCompletion {
  /** Course Module identifier. */
  private Long moduleId;

  /** The unique identifier of the user. */
  private Long userId;

  /** Completion state of the activity for the user. */
  private State state;

  /** Date and time when the activity was completed. Null if it has not been completed. */
  private Long timeCompleted;

  /** How completion is tracked for this activity. */
  private Tracking tracking;

  /** Identifier of the user who manually overrode the status. Null if it was not overridden. */
  private Long overrideBy;

  /** Whether the completion status is used by another activity (e.g. an access restriction). */
  private Boolean valueUsed;

  /** Whether the user is tracked for completion. Depends on the user's role in the course. */
  private Boolean isTrackedUser;

  /** Whether the activity is visible to this specific user. */
  private Boolean userVisible;
}
