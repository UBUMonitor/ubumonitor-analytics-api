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

@Component
@RequiredArgsConstructor
public class SessionAdapter implements SessionPort {

  private final JwtUtils jwtUtils;
  private final SessionStorePort sessionStore;
  private final CurrentSessionContext currentSessionContext;
  private final TenantDatabaseInitializer databaseInitializer;

  @Override
  public JwtToken generateSession(AuthInput authInput) {

    String username = authInput.getUsername();
    String dbPassword = authInput.getDbPassword();
    String moodleToken = authInput.getMoodleToken();
    URI hostUri = authInput.getHost();
    String password = authInput.getPassword();

    if (sessionStore.hasSession(hostUri, username)) {

      SessionData existingSession = sessionStore.getSession(hostUri, username);
      existingSession.setMoodleToken(moodleToken);

      currentSessionContext.setSessionData(existingSession);

      return toJwtToken(existingSession.getJwt());
    }

    SessionData newSession = createSession(username, password, dbPassword, moodleToken, hostUri);

    return toJwtToken(newSession.getJwt());
  }

  private SessionData createSession(
      String username, String password, String dbPassword, String moodleToken, URI hostUri) {

    databaseInitializer.createIfNotExists(hostUri, username, dbPassword);

    String jwt = jwtUtils.generateJwtToken(username, hostUri);

    SessionData sessionData =
        SessionData.builder()
            .jwt(jwt)
            .username(username)
            .password(password)
            .host(hostUri)
            .moodleToken(moodleToken)
            .dbPassword(dbPassword)
            .build();

    sessionStore.saveSession(jwt, sessionData);

    currentSessionContext.setSessionData(sessionData);

    return sessionData;
  }

  private JwtToken toJwtToken(String jwt) {

    return JwtToken.builder().token(jwt).build();
  }
}
