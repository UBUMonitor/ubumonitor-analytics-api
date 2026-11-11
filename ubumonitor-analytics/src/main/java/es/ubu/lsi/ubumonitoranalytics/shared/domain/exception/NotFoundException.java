package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

/** Signals a missing application resource mapped to HTTP 404. */
public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}
