package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;

public interface SessionStorePort {
  void saveSession(String jwt, SessionData sessionData);

  SessionData getSession(String jwt);

  void invalidateSession(String jwt);

  boolean hasSession(URI host, String username);

  SessionData getSession(URI host, String username);
}
