package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConcurrentHashMapSessionStoreAdapter implements SessionStorePort {

  private final ConcurrentHashMap<String, SessionData> sessionCache = new ConcurrentHashMap<>();

  @Override
  public void saveSession(String jwt, SessionData sessionData) {
    sessionCache.put(jwt, sessionData);
  }

  @Override
  public SessionData getSession(String jwt) {
    return sessionCache.get(jwt);
  }

  @Override
  public void invalidateSession(String jwt) {
    sessionCache.remove(jwt);
  }

  @Override
  public boolean hasSession(URI host, String username) {
    return sessionCache.values().stream()
        .anyMatch(
            sessionData ->
                sessionData.getHost().equals(host) && sessionData.getUsername().equals(username));
  }

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
