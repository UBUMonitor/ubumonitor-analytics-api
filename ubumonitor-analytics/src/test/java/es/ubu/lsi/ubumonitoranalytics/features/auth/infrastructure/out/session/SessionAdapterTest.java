package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class SessionAdapterTest {

  @Test
  void buildAuthenticationContextDoesNotCreateOrStoreAnApplicationSession() {
    SessionStorePort sessionStore = mock(SessionStorePort.class);
    SessionAdapterMapper mapper = mock(SessionAdapterMapper.class);
    SessionAdapter sessionAdapter = new SessionAdapter(sessionStore, mapper);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            "moodle-token",
            null);
    RestClient restClient = mock(RestClient.class);
    SessionData temporaryContext =
        SessionData.builder().username("teacher").moodleToken("moodle-token").build();
    when(mapper.toSessionData(authInput, null, null, restClient)).thenReturn(temporaryContext);

    assertEquals(
        temporaryContext, sessionAdapter.buildAuthenticationContext(authInput, restClient));
    verify(sessionStore, never()).saveSession(anyString(), any());
  }

  @Test
  void generateNewSessionReplacesExistingTokenWithFreshToken() {
    SessionStorePort sessionStore = mock(SessionStorePort.class);
    SessionAdapterMapper mapper = mock(SessionAdapterMapper.class);
    SessionAdapter sessionAdapter = new SessionAdapter(sessionStore, mapper);
    URI host = URI.create("https://moodle.test");
    AuthInput authInput =
        new AuthInput("teacher", null, host, "database-password", "moodle-token", null);
    UserInfo userInfo = mock(UserInfo.class);
    RestClient restClient = mock(RestClient.class);
    SessionData oldSession = SessionData.builder().token("old-token").build();
    when(sessionStore.getSession(host, "teacher")).thenReturn(oldSession);
    when(mapper.toSessionData(eq(authInput), anyString(), eq(userInfo), eq(restClient)))
        .thenAnswer(
            invocation ->
                SessionData.builder().token(invocation.getArgument(1, String.class)).build());

    SessionToken result = sessionAdapter.generateNewSession(authInput, userInfo, restClient);

    assertNotEquals("old-token", result.getToken());
    verify(sessionStore).saveSession(eq(result.getToken()), any());
    verify(sessionStore).invalidateSession("old-token");
  }
}
