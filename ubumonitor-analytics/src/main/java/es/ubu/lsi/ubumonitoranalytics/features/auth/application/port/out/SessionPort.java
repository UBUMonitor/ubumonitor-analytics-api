package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import org.springframework.web.client.RestClient;

/** Outbound operations for creating and invalidating application sessions. */
public interface SessionPort {
  /**
   * Creates or reuses a session for the supplied authentication data.
   *
   * @param authInput authenticated Moodle user and site details
   * @param restClient authenticated Moodle web client, if required
   * @return the application session token
   */
  JwtToken generateSession(AuthInput authInput, RestClient restClient);

  /**
   * Replaces an existing session with one based on new authentication data.
   *
   * @param authInput new authenticated Moodle user and site details
   * @param jwtToken token of the session to replace
   * @param restClient authenticated Moodle web client, if required
   * @return the replacement application session token
   */
  JwtToken updateSession(AuthInput authInput, JwtToken jwtToken, RestClient restClient);

  /**
   * Invalidates the session identified by the supplied token.
   *
   * @param jwtToken application session token
   */
  void invalidateSession(JwtToken jwtToken);
}
