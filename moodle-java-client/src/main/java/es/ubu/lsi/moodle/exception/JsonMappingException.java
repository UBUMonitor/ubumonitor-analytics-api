package es.ubu.lsi.moodle.exception;

public class JsonMappingException extends RuntimeException {

  public JsonMappingException(String message, Throwable cause) {
    super(message, cause);
  }

  public JsonMappingException(Throwable cause) {
    super(cause);
  }
}
