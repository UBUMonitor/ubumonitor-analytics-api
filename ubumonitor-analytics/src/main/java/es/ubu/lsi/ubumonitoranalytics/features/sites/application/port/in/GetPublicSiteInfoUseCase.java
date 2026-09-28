package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import java.net.URI;

public interface GetPublicSiteInfoUseCase {
  PublicSiteInfo getPublicSiteInfo(URI host);
}
