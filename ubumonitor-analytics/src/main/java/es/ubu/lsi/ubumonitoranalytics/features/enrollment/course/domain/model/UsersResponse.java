package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsersResponse {
  private Integer actualCourseId;
  private List<User> users;
}
