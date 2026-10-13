package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.SitesApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.PublicSiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.GetPublicSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.GetSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.SyncSiteInfoUseCase;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** Implements the generated REST operations for Moodle site information. */
@Service
@RequiredArgsConstructor
public class SitesApiDelegateImpl implements SitesApiDelegate {

  private final GetSiteInfoUseCase getSiteInfoUseCase;
  private final SyncSiteInfoUseCase syncSiteInfoUseCase;
  private final GetPublicSiteInfoUseCase getPublicSiteInfoUseCase;
  private final SiteInfoMapper siteInfoMapper;

  @Override
  public ResponseEntity<SiteInfoResponseDto> syncSiteInfo() {
    return ResponseEntity.ok(siteInfoMapper.toDto(syncSiteInfoUseCase.syncSiteInfo()));
  }

  @Override
  public ResponseEntity<SiteInfoResponseDto> getSiteInfo() {
    return ResponseEntity.ok(siteInfoMapper.toDto(getSiteInfoUseCase.getIteInfo()));
  }

  @Override
  public ResponseEntity<PublicSiteInfoResponseDto> getPublicSiteInfo(URI host) {
    return ResponseEntity.ok(
        siteInfoMapper.toDto(getPublicSiteInfoUseCase.getPublicSiteInfo(host)));
  }
}
