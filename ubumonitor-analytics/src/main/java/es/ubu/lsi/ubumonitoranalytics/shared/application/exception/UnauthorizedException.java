package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
