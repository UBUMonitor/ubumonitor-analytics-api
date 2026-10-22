package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.request.GetPublicConfigRequestApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Fetches authenticated site and user information from Moodle. */
@Component
@RequiredArgsConstructor
public class SiteInfoApiAdapter implements SiteInfoApiPort {

  private final Client client;
  private final SiteInfoApiAdapterMapper siteInfoApiAdapterMapper;
  private final AsyncOperations asyncOperations;

  @Override
  public SiteInfo fetchSiteInfo() {

    CompletableFuture<GetSiteInfoResponseApi> call1 =
        asyncOperations.supplyAsync(
            () -> client.coreWebservice().getSiteInfo(new GetSiteInfoRequestApi()));

    CompletableFuture<GetPublicConfigResponseApi> call2 =
        asyncOperations.supplyAsync(
            () -> client.toolMobile().getPublicConfig(new GetPublicConfigRequestApi()));

    return siteInfoApiAdapterMapper.toDomain(call1.join(), call2.join());
  }
}
