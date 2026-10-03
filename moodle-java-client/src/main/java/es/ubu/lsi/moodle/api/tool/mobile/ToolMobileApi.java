package es.ubu.lsi.moodle.api.tool.mobile;

import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.request.GetPublicConfigRequestApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;

/** Exposes Moodle mobile-tool operations. */
public interface ToolMobileApi {
  /**
   * @param request public configuration query
   * @return public Moodle mobile configuration
   */
  GetPublicConfigResponseApi getPublicConfig(GetPublicConfigRequestApi request);
}
