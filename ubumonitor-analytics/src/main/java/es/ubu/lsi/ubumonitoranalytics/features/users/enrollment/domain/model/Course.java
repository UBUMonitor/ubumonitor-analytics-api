package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Course {

  private Long id;
  private String fullName;
  private String shortName;
  private Boolean showGrades;
  private Boolean enableCompletion;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime timeModified;
  private Boolean hasBeenLoaded;
}
