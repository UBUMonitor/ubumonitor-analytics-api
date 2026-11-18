package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.OfflineSessionNotAllowedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentMoodleRestClientPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Provides a validated Moodle client for the current online session. */
@Component
@RequiredArgsConstructor
public class CurrentMoodleRestClientAdapter implements CurrentMoodleRestClientPort {

  private final CurrentSessionPort currentSessionPort;

  @Override
  public RestClient requireOnlineRestClient() {
    SessionData sessionData = currentSessionPort.getSessionData();
    if (sessionData == null) {
      throw new IllegalStateException("No current session is available.");
    }
    if (sessionData.getLoginMode() == LoginMode.OFFLINE) {
      throw new OfflineSessionNotAllowedException();
    }

    RestClient restClient = sessionData.getRestClient();
    if (restClient == null) {
      throw new IllegalStateException("No Moodle REST client is available for the online session.");
    }
    return restClient;
  }
}
