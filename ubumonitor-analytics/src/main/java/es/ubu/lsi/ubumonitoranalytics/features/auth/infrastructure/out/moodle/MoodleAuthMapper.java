package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.login.token.request.LoginTokenRequestApi;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps application authentication data to Moodle token requests. */
@Mapper(config = GlobalMapperConfig.class)
public interface MoodleAuthMapper {

  @Mapping(target = "service", ignore = true)
  @Mapping(target = "username", source = "username")
  @Mapping(target = "password", source = "password")
  @Mapping(target = "baseurl", source = "host")
  LoginTokenRequestApi toDto(AuthInput authInput);
}
