package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;

/** Application operations for authenticating a user and creating a session. */
public interface AuthUseCase {

  /**
   * Authenticates with an existing Moodle token.
   *
   * @param tokenAuthInput Moodle token and site details
   * @return the generated session token
   */
  JwtToken loginByToken(AuthInput tokenAuthInput);

  /**
   * Authenticates with Moodle credentials.
   *
   * @param authInput username, password, and site details
   * @return the generated session token
   */
  JwtToken loginByCredentials(AuthInput authInput);

  /**
   * Creates an offline session from the supplied authentication data.
   *
   * @param offlineAuthInput offline authentication details
   * @return the generated session token
   */
  JwtToken loginOffline(AuthInput offlineAuthInput);

  /**
   * Authenticates a Moodle single sign-on request.
   *
   * @param authInput single sign-on authentication details
   * @return the generated session token
   */
  JwtToken loginBySSO(AuthInput authInput);
}
