package es.ubu.lsi.moodle.exception;

public class MoodleUnauthorizedException extends RuntimeException {

  public MoodleUnauthorizedException(String message) {
    super(message);
  }

  public MoodleUnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
