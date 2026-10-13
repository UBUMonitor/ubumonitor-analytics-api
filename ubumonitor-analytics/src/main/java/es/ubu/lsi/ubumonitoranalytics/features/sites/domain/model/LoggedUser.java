package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedUser {

  private Integer id;
  private String username;
  private String fullName;
  private String firstName;
  private String lastName;
  private UserPicture userPicture;
}
