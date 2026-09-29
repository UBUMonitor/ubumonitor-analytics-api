package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

  private final SessionPort sessionPort;
  private final MoodleApiPort moodleApiPort;
  private final ExecutorService executor;

  @Override
  public JwtToken loginByToken(AuthInput authInput) {
    JwtToken jwtToken = sessionPort.generateSession(authInput, null);
    String username = moodleApiPort.usernameByToken(authInput);

    if (username == null || username.isEmpty()) {
      sessionPort.invalidateSession(jwtToken);
      throw new UnauthorizedException("Invalid token.");
    }

    authInput.setUsername(username);

    RestClient restClient = moodleApiPort.getRestClientFromCookies(authInput);

    return sessionPort.updateSession(authInput, jwtToken, restClient);
  }

  @Override
  public JwtToken loginByCredentials(AuthInput authInput) {

    CompletableFuture<String> moodleTokenFuture =
        CompletableFuture.supplyAsync(() -> fetchMoodleToken(authInput), executor);
    CompletableFuture<RestClient> restClientFuture =
        CompletableFuture.supplyAsync(() -> fetchRestClient(authInput), executor);

    String moodleToken = moodleTokenFuture.join();
    RestClient restClient = restClientFuture.join();

    authInput.setMoodleToken(moodleToken);
    return sessionPort.generateSession(authInput, restClient);
  }

  private String fetchMoodleToken(AuthInput authInput) {
    return moodleApiPort.login(authInput);
  }

  private RestClient fetchRestClient(AuthInput authInput) {
    return moodleApiPort.loginWeb(
        authInput.getUsername(), authInput.getPassword(), authInput.getHost());
  }

  @Override
  public JwtToken loginOffline(AuthInput authInput) {

    return sessionPort.generateSession(authInput, null);
  }

  @Override
  public JwtToken loginBySSO(AuthInput authInput) {

    return loginByToken(authInput);
  }
}
