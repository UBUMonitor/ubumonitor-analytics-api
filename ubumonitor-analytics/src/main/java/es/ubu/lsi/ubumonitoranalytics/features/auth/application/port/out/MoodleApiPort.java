package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import java.net.URI;
import org.springframework.web.client.RestClient;

/** Outbound operations used to authenticate with Moodle and create web clients. */
public interface MoodleApiPort {

  /**
   * Requests a Moodle web-service token.
   *
   * @param authInput Moodle credentials and site details
   * @return the Moodle token
   */
  String login(AuthInput authInput);

  /**
   * Resolves the username associated with the current Moodle token.
   *
   * @param authInput authentication details containing the token
   * @return the Moodle username, or {@code null} when it cannot be resolved
   */
  String usernameByToken(AuthInput authInput);

  /**
   * Builds a web client authenticated with the supplied cookies.
   *
   * @param authInput authentication details containing the cookies and site
   * @return a Moodle web client
   */
  RestClient getRestClientFromCookies(AuthInput authInput);

  /**
   * Logs in to Moodle's web interface.
   *
   * @param username Moodle username
   * @param password Moodle password
   * @param host Moodle site URI
   * @return a Moodle web client authenticated for the user
   */
  RestClient loginWeb(String username, String password, URI host);
}
