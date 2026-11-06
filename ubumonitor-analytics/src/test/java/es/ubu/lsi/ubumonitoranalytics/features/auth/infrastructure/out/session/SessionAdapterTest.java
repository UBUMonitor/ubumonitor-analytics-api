package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import org.junit.jupiter.api.Test;

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
}
