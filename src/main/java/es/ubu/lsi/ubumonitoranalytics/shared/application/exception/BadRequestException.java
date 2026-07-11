package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
