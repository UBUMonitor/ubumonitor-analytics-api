package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model;

import lombok.Data;

@Data
public class Enrollment {
  private Course course;
  private Long lastCourseAccess;
  private Boolean isFavourite;
}
