package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.jwt;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.springframework.web.client.RestClient;

/** Maps authentication input into stored session data. */
@Mapper(config = GlobalMapperConfig.class)
public interface SessionAdapterMapper {

  SessionData toSessionData(AuthInput authInput, String jwt, RestClient restClient);
}
