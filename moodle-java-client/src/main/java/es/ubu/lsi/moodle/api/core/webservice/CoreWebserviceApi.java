package es.ubu.lsi.moodle.api.core.webservice;

import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;

public interface CoreWebserviceApi {

  GetSiteInfoResponseApi getSiteInfo(GetSiteInfoRequestApi request);
}
