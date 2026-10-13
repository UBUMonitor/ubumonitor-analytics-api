package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.GetSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Retrieves site information stored for the current authenticated user. */
@Service
@RequiredArgsConstructor
public class GetSiteInfoService implements GetSiteInfoUseCase {

  private final CurrentSessionContext currentSessionContext;
  private final SiteInfoPersistencePort siteInfoPersistencePort;

  /**
   * @return locally stored information for the current Moodle site
   */
  @Override
  @Transactional(readOnly = true)
  public SiteInfo getIteInfo() {
    SessionData sessionData = currentSessionContext.getSessionData();
    return siteInfoPersistencePort.fetchSiteInfo(sessionData.getUsername());
  }
}
