package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.user.getusersbyfield.request.GetUsersByFieldRequestApi;
import es.ubu.lsi.moodle.model.core.user.getusersbyfield.response.GetUsersByFieldResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.SearchUsersByFieldPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.SearchField;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
public class SearchUserByFieldAdapter implements SearchUsersByFieldPort {

  private final Client client;
  private final SearchUserByFieldAdapterMapper mapper;

  @Override
  public UserInfo getUserByUserName(String userName) {
    GetUsersByFieldRequestApi requestApi = mapper.toRequest(SearchField.USERNAME, userName);

    List<GetUsersByFieldResponseApi> responseApiList =
        client.coreUser().getUsersByField(requestApi);

    if (CollectionUtils.isEmpty(responseApiList)) {
      return null;
    }

    return mapper.toResponse(responseApiList.getFirst());
  }
}
