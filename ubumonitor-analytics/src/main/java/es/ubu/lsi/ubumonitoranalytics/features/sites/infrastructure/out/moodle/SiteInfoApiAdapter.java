package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Fetches authenticated site and user information from Moodle. */
@Component
@RequiredArgsConstructor
public class SiteInfoApiAdapter implements SiteInfoApiPort {

  private final Client client;
  private final SiteInfoApiAdapterMapper siteInfoApiAdapterMapper;
  private final AsyncOperations asyncOperations;
  private final CurrentSessionContext currentSessionContext;

  @Override
  public SiteInfo fetchSiteInfo() {

    GetSiteInfoResponseApi response =
        client.coreWebservice().getSiteInfo(new GetSiteInfoRequestApi());

    return siteInfoApiAdapterMapper.toDomain(response);
  }
}
