package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEnrolledCourses {
  private Integer userId;
  private List<Enrollment> enrolledCourses;
}
