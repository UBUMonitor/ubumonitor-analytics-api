package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import java.net.URI;
import org.springframework.web.client.RestClient;

public interface MoodleApiPort {

  String login(AuthInput authInput);

  String usernameByToken(AuthInput authInput);

  RestClient getRestClientFromCookies(AuthInput authInput);

  RestClient loginWeb(String username, String password, URI host);
}
