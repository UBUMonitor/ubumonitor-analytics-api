package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import org.springframework.web.client.RestClient;

public interface SessionPort {
  JwtToken generateSession(AuthInput authInput, RestClient restClient);

  JwtToken updateSession(AuthInput authInput, JwtToken jwtToken, RestClient restClient);

  void invalidateSession(JwtToken jwtToken);
}
