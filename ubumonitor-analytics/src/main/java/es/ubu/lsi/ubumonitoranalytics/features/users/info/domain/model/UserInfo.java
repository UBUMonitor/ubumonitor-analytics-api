package es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model;

import java.net.URI;
import java.time.ZoneId;
import lombok.Data;

@Data
public class UserInfo {
  private Long id;
  private String userName;
  private String firstName;
  private String lastName;
  private String fullName;
  private String initials;
  private String email;
  private ZoneId timeZone;
  private boolean isServerTimeZone;
  private URI userImageUrl;
  private String lang;
}
