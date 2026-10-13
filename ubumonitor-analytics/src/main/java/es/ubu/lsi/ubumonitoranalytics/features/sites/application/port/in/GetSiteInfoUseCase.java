package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;

/** Retrieves site information for the current authenticated user. */
public interface GetSiteInfoUseCase {

  /**
   * @return site and current-user information
   */
  SiteInfo getIteInfo();
}
