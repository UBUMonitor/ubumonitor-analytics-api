package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserCourse {
  private Course course;
  private LocalDateTime lastCourseAccess;
  private Boolean isFavourite;
}
