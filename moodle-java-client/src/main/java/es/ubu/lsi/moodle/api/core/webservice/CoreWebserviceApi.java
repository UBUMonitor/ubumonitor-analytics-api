package es.ubu.lsi.moodle.api.core.webservice;

import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;

/** Exposes Moodle core web-service operations. */
public interface CoreWebserviceApi {

  /**
   * @param request site-information request
   * @return site and current-user details returned by Moodle
   */
  GetSiteInfoResponseApi getSiteInfo(GetSiteInfoRequestApi request);
}
