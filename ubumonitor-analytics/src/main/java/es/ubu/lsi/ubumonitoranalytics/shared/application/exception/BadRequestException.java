package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

/** Signals an application request error mapped to HTTP 400. */
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
