package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Enrollment {
  private Course course;
  private LocalDateTime lastCourseAccess;
  private Boolean isFavourite;
}
