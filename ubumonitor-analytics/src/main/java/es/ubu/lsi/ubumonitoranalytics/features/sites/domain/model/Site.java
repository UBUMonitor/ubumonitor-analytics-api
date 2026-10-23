package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import java.net.URI;
import lombok.Data;

@Data
public class Site {
  private Long id;
  private URI host;
  private String siteName;
  private String versionNumber;
}
