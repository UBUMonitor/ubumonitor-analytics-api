package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import java.net.URI;

/** Retrieves public information about a Moodle site. */
public interface GetPublicSiteInfoUseCase {
  /**
   * @param host Moodle site URI
   * @return public site information
   */
  PublicSiteInfo getPublicSiteInfo(URI host);
}
