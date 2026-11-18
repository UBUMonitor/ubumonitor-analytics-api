package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseAssignment {
  private final Long id;
  private final Long courseId;
  private final Long moduleId;
  private final String name;
  private final String intro;
  private final Long timeModified;
  private final Long dueDate;
  private final Long submissionsFromDate;
  private final Long cutoffDate;
  private final Long gradingDueDate;
}
