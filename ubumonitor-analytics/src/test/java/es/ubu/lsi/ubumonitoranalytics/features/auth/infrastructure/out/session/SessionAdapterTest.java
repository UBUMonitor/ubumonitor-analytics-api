package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class SessionAdapterTest {

  @Test
  void updateSessionDataPreservesTheExistingToken() {
    SessionStorePort sessionStore = mock(SessionStorePort.class);
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    SessionAdapterMapper mapper = mock(SessionAdapterMapper.class);
    SessionAdapter sessionAdapter = new SessionAdapter(sessionStore, currentSessionContext, mapper);
    URI host = URI.create("https://moodle.test");
    AuthInput authInput = new AuthInput("teacher", null, host, "database-password", null, null);
    UserInfo userInfo = mock(UserInfo.class);
    SessionData updatedSession = SessionData.builder().token("existing-token").build();
    SessionToken sessionToken = SessionToken.builder().token("existing-token").build();
    when(mapper.toSessionData(authInput, "existing-token", userInfo, null))
        .thenReturn(updatedSession);

    sessionAdapter.updateSessionData(authInput, sessionToken, userInfo, null);

    assertEquals("existing-token", sessionToken.getToken());
    verify(sessionStore).saveSession("existing-token", updatedSession);
    assertEquals(updatedSession, currentSessionContext.getSessionData());
    currentSessionContext.clear();
  }

  @Test
  void generateSessionReusesTokenForMatchingOpenSession() {
    SessionStorePort sessionStore = mock(SessionStorePort.class);
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    SessionAdapterMapper mapper = mock(SessionAdapterMapper.class);
    SessionAdapter sessionAdapter = new SessionAdapter(sessionStore, currentSessionContext, mapper);
    URI host = URI.create("https://moodle.test");
    AuthInput authInput =
        new AuthInput("teacher", null, host, "database-password", "moodle-token", null);
    RestClient restClient = mock(RestClient.class);
    SessionData existingSession = SessionData.builder().token("existing-token").build();
    when(sessionStore.hasSession(host, "teacher")).thenReturn(true);
    when(sessionStore.getSession(host, "teacher")).thenReturn(existingSession);

    SessionPort.SessionGeneration result =
        sessionAdapter.generateSessionWithStatus(authInput, null, restClient);

    assertEquals("existing-token", result.sessionToken().getToken());
    assertFalse(result.created());
    assertEquals("moodle-token", existingSession.getMoodleToken());
    verify(sessionStore, never()).invalidateSession("existing-token");
    assertEquals(existingSession, currentSessionContext.getSessionData());
    currentSessionContext.clear();
  }

  @Test
  void generateSessionCreatesTokenWhenNoMatchingOpenSessionExists() {
    SessionStorePort sessionStore = mock(SessionStorePort.class);
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    SessionAdapterMapper mapper = mock(SessionAdapterMapper.class);
    SessionAdapter sessionAdapter = new SessionAdapter(sessionStore, currentSessionContext, mapper);
    URI host = URI.create("https://moodle.test");
    AuthInput authInput = new AuthInput("teacher", null, host, "database-password", null, null);
    UserInfo userInfo = mock(UserInfo.class);
    RestClient restClient = mock(RestClient.class);
    when(sessionStore.hasSession(host, "teacher")).thenReturn(false);
    when(mapper.toSessionData(eq(authInput), anyString(), eq(userInfo), eq(restClient)))
        .thenAnswer(
            invocation ->
                SessionData.builder().token(invocation.getArgument(1, String.class)).build());

    SessionPort.SessionGeneration result =
        sessionAdapter.generateSessionWithStatus(authInput, userInfo, restClient);

    assertNotEquals("", result.sessionToken().getToken());
    assertTrue(result.created());
    verify(sessionStore).saveSession(eq(result.sessionToken().getToken()), any());
    assertEquals(
        result.sessionToken().getToken(), currentSessionContext.getSessionData().getToken());
    currentSessionContext.clear();
  }
}
