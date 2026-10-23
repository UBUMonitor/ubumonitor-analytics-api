package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import java.net.URI;
import lombok.Data;

@Data
public class SiteConfig {
  private TypeOfLogin typeOfLogin;
  private Boolean enableWebServices;
  private Boolean enableMobileWebService;
  private URI launchUrl;
  private Boolean maintenanceEnabled;
  private String maintenanceMessage;
}
