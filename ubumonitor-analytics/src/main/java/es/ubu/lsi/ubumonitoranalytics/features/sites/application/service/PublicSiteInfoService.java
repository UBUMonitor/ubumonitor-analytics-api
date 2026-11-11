package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.GetPublicSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.PublicSiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Retrieves public configuration from the Moodle site supplied by the caller. */
@Service
@RequiredArgsConstructor
public class PublicSiteInfoService implements GetPublicSiteInfoUseCase {
  private final PublicSiteInfoApiPort siteInfoApiPort;

  /**
   * @param host Moodle site URI
   * @return public configuration returned by Moodle
   */
  @Override
  public PublicSiteInfo getPublicSiteInfo(URI host) {
    SessionData sessionData = SessionData.builder().host(host).build();
    return siteInfoApiPort.fetchPublicSiteInfo(sessionData);
  }
}
