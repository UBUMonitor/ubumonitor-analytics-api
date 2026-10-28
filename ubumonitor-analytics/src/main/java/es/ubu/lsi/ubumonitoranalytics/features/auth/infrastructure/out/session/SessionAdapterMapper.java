package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.session;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.client.RestClient;

/** Maps authentication input into stored session data. */
@Mapper(config = GlobalMapperConfig.class)
public interface SessionAdapterMapper {

  @Mapping(target = "userId", source = "userInfo.id")
  @Mapping(target = "username", source = "authInput.username")
  @Mapping(target = "actualUser", source = "userInfo")
  @Mapping(target = "token", source = "token")
  SessionData toSessionData(
      AuthInput authInput, String token, UserInfo userInfo, RestClient restClient);
}
