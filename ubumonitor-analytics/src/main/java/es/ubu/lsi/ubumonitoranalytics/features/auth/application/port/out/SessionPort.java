package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import org.springframework.web.client.RestClient;

/** Outbound operations for preparing session data and creating sessions. */
public interface SessionPort {

  /**
   * Builds and activates authentication and tenant details for downstream work without creating an
   * application session.
   *
   * @param authInput Moodle authentication and tenant details
   * @param restClient authenticated Moodle web client, if required
   * @return session data for the active authentication context
   */
  SessionData activateAuthenticationContext(AuthInput authInput, RestClient restClient);

  /**
   * Creates a new application session and replaces any current session for the same site and user.
   *
   * @param authInput authenticated Moodle user and site details
   * @param userInfo synchronized current-user information
   * @param restClient authenticated Moodle web client, if required
   * @return the newly created application session token
   */
  SessionToken generateNewSession(AuthInput authInput, UserInfo userInfo, RestClient restClient);

  /**
   * Invalidates the session identified by the supplied token.
   *
   * @param sessionToken application session token
   */
  void invalidateSession(SessionToken sessionToken);
}
