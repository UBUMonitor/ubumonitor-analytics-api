package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthLoginRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthOfflineRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthSSORequestDto;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AuthMapperTest {

  private final AuthMapper mapper = Mappers.getMapper(AuthMapper.class);

  @Test
  void mapsLoginCredentialsToCredentialsMode() {
    assertEquals(LoginMode.CREDENTIALS, mapper.toDomain(new AuthLoginRequestDto()).getLoginMode());
  }

  @Test
  void mapsOfflineLoginToOfflineMode() {
    assertEquals(LoginMode.OFFLINE, mapper.toDomain(new AuthOfflineRequestDto()).getLoginMode());
  }

  @Test
  void mapsSsoLoginToSsoMode() {
    assertEquals(LoginMode.SSO, mapper.toDomain(new AuthSSORequestDto()).getLoginMode());
  }
}
