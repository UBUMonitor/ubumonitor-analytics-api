package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaffeineSessionStoreAdapter implements SessionStorePort {

  private final Cache<String, SessionData> sessionCache =
      Caffeine.newBuilder().maximumSize(1000).build();

  @Override
  public void saveSession(String jwt, SessionData sessionData) {
    sessionCache.put(jwt, sessionData);
  }

  @Override
  public SessionData getSession(String jwt) {
    return sessionCache.getIfPresent(jwt);
  }

  @Override
  public void invalidateSession(String jwt) {
    sessionCache.invalidate(jwt);
  }

  @Override
  public boolean hasSession(URI host, String username) {
    return sessionCache.asMap().values().stream()
        .anyMatch(
            sessionData ->
                sessionData.getHost().equals(host) && sessionData.getUsername().equals(username));
  }

  @Override
  public SessionData getSession(URI host, String username) {
    return sessionCache.asMap().values().stream()
        .filter(
            sessionData ->
                sessionData.getHost().equals(host) && sessionData.getUsername().equals(username))
        .findAny()
        .orElse(null);
  }
}
