package es.ubu.lsi.moodle.api.core.user;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.user.getusersbyfield.request.GetUsersByFieldRequestApi;
import es.ubu.lsi.moodle.model.core.user.getusersbyfield.response.GetUsersByFieldResponseApi;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoreUserApiImpl implements CoreUserApi {
  private final Client client;

  @Override
  public List<GetUsersByFieldResponseApi> getUsersByField(GetUsersByFieldRequestApi request) {
    return client.executeList(request, GetUsersByFieldResponseApi.class);
  }
}
