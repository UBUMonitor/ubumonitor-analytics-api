package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

/** Signals an application state conflict mapped to HTTP 409. */
public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
