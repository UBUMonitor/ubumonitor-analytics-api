package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;

/** Stores and looks up active application sessions. */
public interface SessionStorePort {
  /**
   * @param token application session token
   * @param sessionData session state to store
   */
  void saveSession(String token, SessionData sessionData);

  /**
   * @param token application session token
   * @return matching session state, or {@code null} when absent
   */
  SessionData getSession(String token);

  /**
   * @param token application session token to invalidate
   */
  void invalidateSession(String token);

  /**
   * @param host Moodle site URI
   * @param username Moodle username
   * @return whether an active session exists for the user and site
   */
  boolean hasSession(URI host, String username);

  /**
   * @param host Moodle site URI
   * @param username Moodle username
   * @return matching session state, or {@code null} when absent
   */
  SessionData getSession(URI host, String username);
}
