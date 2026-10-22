package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedUser {

  private Long id;
  private String username;
  private String fullName;
  private String firstName;
  private String lastName;
  private ZoneId timeZone;
  private UserPicture userPicture;
}
