package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

import java.net.URI;
import java.time.ZoneId;
import lombok.Data;

@Data
public class ActualUser {

  private String firstName;
  private String lastName;
  private String fullName;
  private String initials;
  private String email;
  private ZoneId timeZone;
  private URI userImageUrl;
  private String lang;
}
