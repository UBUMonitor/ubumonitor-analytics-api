package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;

/** Fetches unauthenticated public configuration from Moodle. */
public interface PublicSiteInfoApiPort {

  /**
   * @return public Moodle site information
   */
  PublicSiteInfo fetchPublicSiteInfo();
}
