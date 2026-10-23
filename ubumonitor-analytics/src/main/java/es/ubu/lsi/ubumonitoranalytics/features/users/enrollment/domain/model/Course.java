package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class Course {

  private Long id;
  private String fullName;
  private String shortName;
  private Boolean showGrades;
  private Boolean enableCompletion;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private OffsetDateTime timeModified;
  private Boolean hasBeenLoaded;
}
