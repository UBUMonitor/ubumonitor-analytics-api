package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;

/** Reads and stores site information in the current tenant database. */
public interface SiteInfoPersistencePort {

  /**
   * @param username current Moodle username
   * @return site information stored for that user, or {@code null} when absent
   */
  SiteInfo fetchSiteInfo(String username);

  /**
   * @param site site information to persist
   */
  void save(SiteInfo site);
}
