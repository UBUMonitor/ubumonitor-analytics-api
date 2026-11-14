package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.OfflineSessionNotAllowedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class RestClientHttpTransportTest {

  private final RestClient restClient = mock(RestClient.class);
  private final CurrentSessionPort currentSessionContext = mock(CurrentSessionPort.class);
  private final RestClientHttpTransport transport =
      new RestClientHttpTransport(restClient, currentSessionContext);

  @Test
  void rejectsOfflineSessionBeforeSendingMoodleRequest() {
    when(currentSessionContext.getSessionData())
        .thenReturn(SessionData.builder().loginMode(LoginMode.OFFLINE).build());

    assertThrows(
        OfflineSessionNotAllowedException.class,
        () -> transport.postForm("webservice/rest/server.php", Map.of(), Object.class));

    verifyNoInteractions(restClient);
  }
}
