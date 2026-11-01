package es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model;

import java.time.ZoneId;
import lombok.Data;

@Data
public class AuthCurrentUser {
  private Long id;
  private String userName;
  private String firstName;
  private String lastName;
  private String fullName;
  private String initials;
  private String email;
  private ZoneId timeZone;
  private String lang;
}
