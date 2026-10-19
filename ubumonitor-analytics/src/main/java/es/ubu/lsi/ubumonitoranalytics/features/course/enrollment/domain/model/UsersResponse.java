package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsersResponse {
  private Long actualCourseId;
  private List<User> users;
}
