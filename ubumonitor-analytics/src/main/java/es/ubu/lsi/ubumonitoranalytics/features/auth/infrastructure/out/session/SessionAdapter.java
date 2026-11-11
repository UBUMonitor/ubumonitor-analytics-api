package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.session;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Implements session creation and invalidation using the shared session store. */
@Component
@RequiredArgsConstructor
public class SessionAdapter implements SessionPort {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final int TOKEN_BYTES = 32;

  private final SessionStorePort sessionStore;
  private final SessionAdapterMapper sessionAdapterMapper;

  @Override
  public SessionData buildAuthenticationContext(AuthInput authInput, RestClient restClient) {
    return sessionAdapterMapper.toSessionData(authInput, null, null, restClient);
  }

  @Override
  public synchronized SessionToken generateNewSession(
      AuthInput authInput, UserInfo userInfo, RestClient restClient) {
    SessionData existingSession =
        sessionStore.getSession(authInput.getHost(), authInput.getUsername());
    SessionData newSession = createSession(authInput, userInfo, restClient);
    if (existingSession != null && existingSession.getToken() != null) {
      sessionStore.invalidateSession(existingSession.getToken());
    }
    return toSessionToken(newSession.getToken());
  }

  @Override
  public void invalidateSession(SessionToken sessionToken) {
    sessionStore.invalidateSession(sessionToken.getToken());
  }

  private SessionData createSession(AuthInput authInput, UserInfo userInfo, RestClient restClient) {

    String token = generateToken();

    SessionData sessionData =
        sessionAdapterMapper.toSessionData(authInput, token, userInfo, restClient);

    sessionStore.saveSession(token, sessionData);

    return sessionData;
  }

  private SessionToken toSessionToken(String token) {

    return SessionToken.builder().token(token).build();
  }

  private String generateToken() {
    byte[] tokenBytes = new byte[TOKEN_BYTES];
    SECURE_RANDOM.nextBytes(tokenBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }
}
