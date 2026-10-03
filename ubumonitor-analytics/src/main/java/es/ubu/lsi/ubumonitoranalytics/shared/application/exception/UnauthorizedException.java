package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

/** Signals failed application authentication mapped to HTTP 401. */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
