package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;

/** Application operations for authenticating a user and creating a session. */
public interface AuthUseCase {

  /**
   * Authenticates with Moodle credentials.
   *
   * @param authInput username, password, and site details
   * @return the generated session token
   */
  SessionToken loginByCredentials(AuthInput authInput);

  /**
   * Creates an offline session from the supplied authentication data.
   *
   * @param offlineAuthInput offline authentication details
   * @return the generated session token
   */
  SessionToken loginOffline(AuthInput offlineAuthInput);

  /**
   * Authenticates a Moodle single sign-on request.
   *
   * @param authInput single sign-on authentication details
   * @return the generated session token
   */
  SessionToken loginBySSO(AuthInput authInput);
}
