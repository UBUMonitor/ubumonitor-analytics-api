package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;

/** Synchronizes information about the current Moodle site. */
public interface SyncSiteInfoUseCase {
  /**
   * @return the site information fetched from Moodle and stored locally
   */
  SiteInfo syncSiteInfo();
}
