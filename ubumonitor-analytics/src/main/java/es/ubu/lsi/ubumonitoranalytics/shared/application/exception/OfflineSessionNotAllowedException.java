package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

/** Raised when an operation requires a Moodle connection but the session is offline. */
public class OfflineSessionNotAllowedException extends RuntimeException {

  public OfflineSessionNotAllowedException() {
    super("An online Moodle session is required for this operation.");
  }
}
