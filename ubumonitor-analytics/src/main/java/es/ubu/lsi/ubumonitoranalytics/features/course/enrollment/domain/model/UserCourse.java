package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class UserCourse {
  private Course course;
  private OffsetDateTime lastCourseAccess;
  private Boolean isFavourite;
}
