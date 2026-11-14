package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.OfflineSessionNotAllowedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class CurrentMoodleRestClientAdapterTest {

  private final CurrentSessionPort currentSessionPort = mock(CurrentSessionPort.class);
  private final CurrentMoodleRestClientAdapter adapter =
      new CurrentMoodleRestClientAdapter(currentSessionPort);

  @Test
  void rejectsOfflineSession() {
    when(currentSessionPort.getSessionData())
        .thenReturn(SessionData.builder().loginMode(LoginMode.OFFLINE).build());

    assertThrows(OfflineSessionNotAllowedException.class, adapter::requireOnlineRestClient);
  }

  @Test
  void reportsMissingRestClientForOnlineSession() {
    when(currentSessionPort.getSessionData())
        .thenReturn(SessionData.builder().loginMode(LoginMode.CREDENTIALS).build());

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, adapter::requireOnlineRestClient);

    org.junit.jupiter.api.Assertions.assertEquals(
        "No Moodle REST client is available for the online session.", exception.getMessage());
  }

  @Test
  void returnsRestClientForOnlineSession() {
    RestClient restClient = mock(RestClient.class);
    when(currentSessionPort.getSessionData())
        .thenReturn(SessionData.builder().loginMode(LoginMode.SSO).restClient(restClient).build());

    assertSame(restClient, adapter.requireOnlineRestClient());
  }

  @Test
  void reportsMissingCurrentSession() {
    when(currentSessionPort.getSessionData()).thenReturn(null);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, adapter::requireOnlineRestClient);

    org.junit.jupiter.api.Assertions.assertEquals(
        "No current session is available.", exception.getMessage());
  }
}
