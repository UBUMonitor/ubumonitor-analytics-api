package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.ajax.AjaxRequest;
import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.request.GetPublicConfigArgsApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.request.GetPublicConfigRequestApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.PublicSiteInfoApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PublicSiteInfoApiAdapter implements PublicSiteInfoApiPort {

  private final Client client;
  private final SiteInfoApiAdapterMapper siteInfoApiAdapterMapper;

  @Override
  public PublicSiteInfo fetchPublicSiteInfo() {
    GetPublicConfigRequestApi request = new GetPublicConfigRequestApi();
    request.setArgs(new GetPublicConfigArgsApi());
    AjaxResponse<GetPublicConfigResponseApi> response =
        client.executeAjax(
            new AjaxRequest<>(request.getWsfunction(), request.getArgs()),
            GetPublicConfigResponseApi.class);

    if (response.getError()) {
      throw new RuntimeException("Error fetching public site info");
    }

    return siteInfoApiAdapterMapper.toDomain(response.getData());
  }
}
