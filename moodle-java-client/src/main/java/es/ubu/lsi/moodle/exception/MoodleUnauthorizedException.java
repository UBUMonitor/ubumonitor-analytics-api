package es.ubu.lsi.moodle.exception;

/** Indicates that Moodle rejected the supplied authentication. */
public class MoodleUnauthorizedException extends RuntimeException {

  /**
   * @param message authentication failure detail returned by Moodle
   */
  public MoodleUnauthorizedException(String message) {
    super(message);
  }

  /**
   * @param message authentication failure detail returned by Moodle
   * @param cause underlying response or transport failure
   */
  public MoodleUnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
