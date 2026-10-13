package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;

/** Fetches authenticated site information from Moodle. */
public interface SiteInfoApiPort {

  /**
   * @return site and current-user information returned by Moodle
   */
  SiteInfo fetchSiteInfo();
}
