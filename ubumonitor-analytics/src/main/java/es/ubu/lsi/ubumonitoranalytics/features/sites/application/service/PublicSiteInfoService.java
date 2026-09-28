package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.GetPublicSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.PublicSiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PublicSiteInfoService implements GetPublicSiteInfoUseCase {
  private final PublicSiteInfoApiPort siteInfoApiPort;
  private final CurrentSessionContext currentSessionContext;

  @Override
  public PublicSiteInfo getPublicSiteInfo(URI host) {
    SessionData sessionData = SessionData.builder().host(host).build();
    currentSessionContext.setSessionData(sessionData);
    return siteInfoApiPort.fetchPublicSiteInfo();
  }
}
