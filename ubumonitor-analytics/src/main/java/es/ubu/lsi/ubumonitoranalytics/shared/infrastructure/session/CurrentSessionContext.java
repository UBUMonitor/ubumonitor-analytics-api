package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import org.springframework.stereotype.Component;

/** Exposes the session data associated with the current request. */
@Component
public class CurrentSessionContext {

  private static final ThreadLocal<SessionData> SESSION = new ThreadLocal<>();

  /**
   * Associates session data with the current request thread.
   *
   * @param data session data to store
   */
  public void setSessionData(SessionData data) {
    SESSION.set(data);
  }

  /**
   * Returns session data associated with the current request thread.
   *
   * @return current session data, or {@code null} when no session is set
   */
  public SessionData getSessionData() {
    return SESSION.get();
  }

  /** Removes session data from the current request thread. */
  public void clear() {
    SESSION.remove();
  }
}
