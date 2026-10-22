package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.Data;

@Data
public class Course {

  private Long id;
  private String fullName;
  private String shortName;
  private Boolean hasBeenLoaded;
}
