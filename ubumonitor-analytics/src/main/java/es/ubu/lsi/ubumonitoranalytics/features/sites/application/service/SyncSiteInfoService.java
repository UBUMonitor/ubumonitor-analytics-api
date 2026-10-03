package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.SyncSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fetches Moodle site information and persists it for the current tenant. */
@Service
@RequiredArgsConstructor
public class SyncSiteInfoService implements SyncSiteInfoUseCase {

  private final SiteInfoApiPort siteInfoApiPort;
  private final SiteInfoPersistencePort siteInfoPersistencePort;

  /**
   * @return site information fetched from Moodle and saved locally
   */
  @Override
  @Transactional
  public SiteInfo syncSiteInfo() {
    SiteInfo siteInfo = siteInfoApiPort.fetchSiteInfo();
    siteInfoPersistencePort.save(siteInfo);
    return siteInfo;
  }
}
