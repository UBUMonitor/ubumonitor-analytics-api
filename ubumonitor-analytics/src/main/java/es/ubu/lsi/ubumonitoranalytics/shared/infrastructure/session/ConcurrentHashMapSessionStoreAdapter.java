package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Stores active session data in a concurrent in-memory map. */
@Component
@RequiredArgsConstructor
public class ConcurrentHashMapSessionStoreAdapter implements SessionStorePort {

  private final ConcurrentHashMap<String, SessionData> sessionCache = new ConcurrentHashMap<>();

  /**
   * Stores session data under its application JWT.
   *
   * @param jwt application session token
   * @param sessionData data associated with the session
   */
  @Override
  public void saveSession(String jwt, SessionData sessionData) {
    sessionCache.put(jwt, sessionData);
  }

  /**
   * Looks up session data by application JWT.
   *
   * @param jwt application session token
   * @return stored session data, or {@code null} when absent
   */
  @Override
  public SessionData getSession(String jwt) {
    return sessionCache.get(jwt);
  }

  /**
   * Removes a session from the in-memory store.
   *
   * @param jwt application session token
   */
  @Override
  public void invalidateSession(String jwt) {
    sessionCache.remove(jwt);
  }

  /**
   * Checks whether a session exists for a Moodle host and username.
   *
   * @param host Moodle site URI
   * @param username Moodle username
   * @return whether a matching session exists
   */
  @Override
  public boolean hasSession(URI host, String username) {
    return sessionCache.values().stream()
        .anyMatch(
            sessionData ->
                sessionData.getHost().equals(host) && sessionData.getUsername().equals(username));
  }

  /**
   * Finds a session for a Moodle host and username.
   *
   * @param host Moodle site URI
   * @param username Moodle username
   * @return matching session data, or {@code null} when absent
   */
  @Override
  public SessionData getSession(URI host, String username) {
    return sessionCache.values().stream()
        .filter(
            sessionData ->
                sessionData.getHost().equals(host) && sessionData.getUsername().equals(username))
        .findAny()
        .orElse(null);
  }
}
