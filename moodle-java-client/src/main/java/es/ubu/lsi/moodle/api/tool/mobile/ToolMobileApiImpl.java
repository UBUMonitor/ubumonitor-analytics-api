package es.ubu.lsi.moodle.api.tool.mobile;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.request.GetPublicConfigRequestApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;
import lombok.RequiredArgsConstructor;

/** Implements Moodle mobile-tool calls through the shared client. */
@RequiredArgsConstructor
public class ToolMobileApiImpl implements ToolMobileApi {
  private final Client client;

  @Override
  public GetPublicConfigResponseApi getPublicConfig(GetPublicConfigRequestApi request) {
    return client.execute(request, GetPublicConfigResponseApi.class);
  }
}
