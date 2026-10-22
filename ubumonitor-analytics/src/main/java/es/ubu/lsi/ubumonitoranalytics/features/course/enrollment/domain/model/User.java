package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class User {
  private Long id;
  private String fullName;
  private String firstName;
  private String lastName;
  private String email;
  private LocalDateTime firstAccess;
  private LocalDateTime lastAccess;
  private UserPicture userPicture;
  private List<Role> roles;
  private List<Group> groups;
  private List<UserCourse> courses;
}
