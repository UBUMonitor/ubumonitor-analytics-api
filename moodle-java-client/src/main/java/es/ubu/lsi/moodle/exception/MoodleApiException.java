package es.ubu.lsi.moodle.exception;

public class MoodleApiException extends RuntimeException {

  public MoodleApiException(String message) {
    super(message);
  }

  public MoodleApiException(Throwable cause) {
    super(cause);
  }

  public MoodleApiException(String message, Throwable e) {
    super(message, e);
  }
}
