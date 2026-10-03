package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database.TenantDatabasePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Coordinates Moodle authentication and application session creation. */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

  private final SessionPort sessionPort;
  private final MoodleApiPort moodleApiPort;
  private final TenantDatabasePort tenantDatabasePort;
  private final ExecutorService executor;

  /**
   * Authenticates with an existing Moodle token and associates a web client with the session.
   *
   * @param authInput Moodle token, site, and optional cookies
   * @return the application session token
   * @throws UnauthorizedException when Moodle does not resolve the token to a username
   */
  @Override
  public JwtToken loginByToken(AuthInput authInput) {
    JwtToken jwtToken = sessionPort.generateSession(authInput, null);
    String username = moodleApiPort.usernameByToken(authInput);

    if (username == null || username.isEmpty()) {
      sessionPort.invalidateSession(jwtToken);
      throw new UnauthorizedException("Invalid token.");
    }

    authInput.setUsername(username);
    initializeTenantDatabaseAsync(authInput);

    RestClient restClient = moodleApiPort.getRestClientFromCookies(authInput);

    return sessionPort.updateSession(authInput, jwtToken, restClient);
  }

  /**
   * Authenticates with Moodle credentials and creates an application session.
   *
   * @param authInput Moodle username, password, and site
   * @return the application session token
   */
  @Override
  public JwtToken loginByCredentials(AuthInput authInput) {

    initializeTenantDatabaseAsync(authInput);

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

  /**
   * Creates an application session without an authenticated Moodle web client.
   *
   * @param authInput offline authentication details
   * @return the application session token
   */
  @Override
  public JwtToken loginOffline(AuthInput authInput) {

    initializeTenantDatabaseAsync(authInput);
    return sessionPort.generateSession(authInput, null);
  }

  private void initializeTenantDatabaseAsync(AuthInput authInput) {
    SessionData tenantSession =
        SessionData.builder()
            .host(authInput.getHost())
            .username(authInput.getUsername())
            .dbPassword(authInput.getDbPassword())
            .build();
    tenantDatabasePort.initializeTenantAsync(tenantSession);
  }

  /**
   * Authenticates a single sign-on request using the supplied Moodle token.
   *
   * @param authInput single sign-on authentication details
   * @return the application session token
   */
  @Override
  public JwtToken loginBySSO(AuthInput authInput) {

    return loginByToken(authInput);
  }
}
