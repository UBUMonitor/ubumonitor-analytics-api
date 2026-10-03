package es.ubu.lsi.moodle.exception;

/** Indicates that a Moodle JSON response could not be mapped to its model. */
public class JsonMappingException extends RuntimeException {

  /**
   * @param message mapping failure detail
   * @param cause underlying serialization or deserialization failure
   */
  public JsonMappingException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param cause underlying serialization or deserialization failure
   */
  public JsonMappingException(Throwable cause) {
    super(cause);
  }
}
