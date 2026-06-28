package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class Enrollment {
  private Course course;
  private OffsetDateTime lastCourseAccess;
  private Boolean isFavourite;
}
