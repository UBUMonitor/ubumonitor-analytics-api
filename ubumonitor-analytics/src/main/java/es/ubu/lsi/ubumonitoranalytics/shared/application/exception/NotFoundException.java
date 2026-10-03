package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

/** Signals a missing application resource mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}
