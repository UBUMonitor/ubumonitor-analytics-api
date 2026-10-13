package es.ubu.lsi.moodle.exception;

/** Indicates that a Moodle API request failed. */
public class MoodleApiException extends RuntimeException {

  /**
   * @param message failure detail
   */
  public MoodleApiException(String message) {
    super(message);
  }

  /**
   * @param cause underlying request or mapping failure
   */
  public MoodleApiException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message failure detail
   * @param e underlying request or mapping failure
   */
  public MoodleApiException(String message, Throwable e) {
    super(message, e);
  }
}
