package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import org.springframework.web.client.RestClient;

/** Outbound operations for creating and invalidating application sessions. */
public interface SessionPort {
  /**
   * Creates or reuses a session for the supplied authentication data.
   *
   * @param authInput authenticated Moodle user and site details
   * @param userInfo user info
   * @param restClient authenticated Moodle web client, if required
   * @return the application session token
   */
  SessionToken generateSession(AuthInput authInput, UserInfo userInfo, RestClient restClient);

  /**
   * Replaces an existing session with one based on new authentication data.
   *
   * @param authInput new authenticated Moodle user and site details
   * @param sessionToken token of the session to replace
   * @param restClient authenticated Moodle web client, if required
   * @return the replacement application session token
   */
  SessionToken updateSession(
      AuthInput authInput, SessionToken sessionToken, UserInfo userInfo, RestClient restClient);

  /**
   * Invalidates the session identified by the supplied token.
   *
   * @param sessionToken application session token
   */
  void invalidateSession(SessionToken sessionToken);
}
