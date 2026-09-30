package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in;

/** Application operation for ending the current session. */
public interface LogoutUseCase {
  /** Invalidates the current session and releases its tenant resources. */
  void logout();
}
