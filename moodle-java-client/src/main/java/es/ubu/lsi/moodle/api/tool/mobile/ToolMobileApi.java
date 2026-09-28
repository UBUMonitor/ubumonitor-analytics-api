package es.ubu.lsi.moodle.api.tool.mobile;

import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.request.GetPublicConfigRequestApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;

public interface ToolMobileApi {
  GetPublicConfigResponseApi getPublicConfig(GetPublicConfigRequestApi request);
}
