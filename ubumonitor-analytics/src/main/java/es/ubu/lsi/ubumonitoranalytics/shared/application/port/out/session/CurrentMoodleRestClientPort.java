package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session;

import org.springframework.web.client.RestClient;

/** Provides the authenticated Moodle client associated with the current session. */
public interface CurrentMoodleRestClientPort {

  /**
   * @return the authenticated Moodle client for the current online session
   * @throws
   *     es.ubu.lsi.ubumonitoranalytics.shared.application.exception.OfflineSessionNotAllowedException
   *     when the current session is offline
   * @throws IllegalStateException when no session or Moodle client is available
   */
  RestClient requireOnlineRestClient();
}
