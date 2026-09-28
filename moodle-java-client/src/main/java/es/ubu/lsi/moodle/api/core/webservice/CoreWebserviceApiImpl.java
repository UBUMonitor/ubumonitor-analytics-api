package es.ubu.lsi.moodle.api.core.webservice;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoreWebserviceApiImpl implements CoreWebserviceApi {

  private final Client client;

  @Override
  public GetSiteInfoResponseApi getSiteInfo(GetSiteInfoRequestApi request) {
    return client.execute(request, GetSiteInfoResponseApi.class);
  }
}
