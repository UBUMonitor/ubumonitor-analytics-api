package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model;

import lombok.Data;

@Data
public class Course {

  private Long id;
  private String fullName;
  private String shortName;
  private Boolean showGrades;
  private Boolean enableCompletion;
  private Long startDate;
  private Long endDate;
  private Long timeModified;
  private Long lastSync;
}
