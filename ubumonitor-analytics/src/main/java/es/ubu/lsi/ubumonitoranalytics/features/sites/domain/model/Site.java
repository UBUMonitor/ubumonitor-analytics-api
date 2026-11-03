package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import java.net.URI;
import lombok.Data;

@Data
public class Site {
  private Long id;
  private Long userId;
  private String userName;
  private String firstName;
  private String lastName;
  private String fullName;
  private URI host;
  private String siteName;
  private String versionNumber;
  private String release;
  private String version;
}
