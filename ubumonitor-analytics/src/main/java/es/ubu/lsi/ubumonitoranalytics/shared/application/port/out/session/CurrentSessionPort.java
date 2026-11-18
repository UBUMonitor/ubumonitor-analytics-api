package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;

/** Provides session data associated with the current thread. */
public interface CurrentSessionPort {

  /**
   * @return session data associated with the current thread, or {@code null} when no session is set
   */
  SessionData getSessionData();
}
