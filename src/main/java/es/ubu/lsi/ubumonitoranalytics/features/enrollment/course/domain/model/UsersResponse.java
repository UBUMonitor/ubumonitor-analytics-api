package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model;

import java.util.Collection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsersResponse {
  private Integer actualCourseId;
  private Collection<User> users;
}
