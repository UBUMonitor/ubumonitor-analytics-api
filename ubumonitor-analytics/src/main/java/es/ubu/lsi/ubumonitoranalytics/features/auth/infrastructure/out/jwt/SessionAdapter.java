package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.jwt;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.TenantDatabaseInitializer;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.JwtUtils;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class SessionAdapter implements SessionPort {

  private final JwtUtils jwtUtils;
  private final SessionStorePort sessionStore;
  private final CurrentSessionContext currentSessionContext;
  private final TenantDatabaseInitializer databaseInitializer;
  private final SessionAdapterMapper sessionAdapterMapper;

  @Override
  public JwtToken generateSession(AuthInput authInput, RestClient restClient) {

    String username = authInput.getUsername();
    String moodleToken = authInput.getMoodleToken();
    URI hostUri = authInput.getHost();

    if (sessionStore.hasSession(hostUri, username)) {

      SessionData existingSession = sessionStore.getSession(hostUri, username);
      existingSession.setMoodleToken(moodleToken);

      currentSessionContext.setSessionData(existingSession);

      return toJwtToken(existingSession.getJwt());
    }

    SessionData newSession = createSession(authInput, restClient);

    return toJwtToken(newSession.getJwt());
  }

  @Override
  public JwtToken updateSession(AuthInput authInput, JwtToken jwtToken, RestClient restClient) {

    invalidateSession(jwtToken);

    SessionData newSession = createSession(authInput, restClient);

    return toJwtToken(newSession.getJwt());
  }

  @Override
  public void invalidateSession(JwtToken jwtToken) {
    sessionStore.invalidateSession(jwtToken.getToken());
  }

  private SessionData createSession(AuthInput authInput, RestClient restClient) {

    String username = authInput.getUsername();
    String dbPassword = authInput.getDbPassword();
    URI hostUri = authInput.getHost();

    databaseInitializer.createIfNotExists(hostUri, username, dbPassword);

    String jwt = jwtUtils.generateJwtToken(username, hostUri);

    SessionData sessionData = sessionAdapterMapper.toSessionData(authInput, jwt, restClient);

    sessionStore.saveSession(jwt, sessionData);

    currentSessionContext.setSessionData(sessionData);

    return sessionData;
  }

  private JwtToken toJwtToken(String jwt) {

    return JwtToken.builder().token(jwt).build();
  }
}
