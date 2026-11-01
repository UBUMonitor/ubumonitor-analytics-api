package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.Data;

@Data
public class UserCourse {
  private Course course;
  private Long lastCourseAccess;
  private Boolean isFavourite;
}
