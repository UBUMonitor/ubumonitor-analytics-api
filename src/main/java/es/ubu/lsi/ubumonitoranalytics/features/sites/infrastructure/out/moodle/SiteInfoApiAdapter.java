package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.request.GetPublicConfigRequestApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SiteInfoApiAdapter implements SiteInfoApiPort {

  private final Client client;
  private final SiteInfoApiAdapterMapper siteInfoApiAdapterMapper;
  private final Executor executor;

  @Override
  public SiteInfo fetchSiteInfo() {

    CompletableFuture<GetSiteInfoResponseApi> call1 =
        CompletableFuture.supplyAsync(
            () -> client.coreWebservice().getSiteInfo(new GetSiteInfoRequestApi()), executor);

    CompletableFuture<GetPublicConfigResponseApi> call2 =
        CompletableFuture.supplyAsync(
            () -> client.toolMobile().getPublicConfig(new GetPublicConfigRequestApi()), executor);
    CompletableFuture.allOf(call1, call2).join();
    return siteInfoApiAdapterMapper.toDomain(call1.join(), call2.join());
  }
}
