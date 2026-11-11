package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import org.springframework.stereotype.Component;

/** Exposes the session data associated with the current request. */
@Component
public class CurrentSessionContext {

  private static final ThreadLocal<SessionData> SESSION = new ThreadLocal<>();

  /** Returns the current session data, or {@code null} when no session is set. */
  public SessionData getSessionData() {
    return SESSION.get();
  }

  /** Sets session data for the current thread. */
  public void setSessionData(SessionData data) {
    SESSION.set(data);
  }

  /** Removes session data from the current thread. */
  public void clear() {
    SESSION.remove();
  }
}
