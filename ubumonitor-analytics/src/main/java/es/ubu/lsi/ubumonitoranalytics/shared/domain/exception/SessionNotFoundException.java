package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

/** Indicates that an application session cannot be found or created. */
public class SessionNotFoundException extends RuntimeException {

  public SessionNotFoundException(String jwt) {
    super("Session not found for token: " + jwt);
  }

  public SessionNotFoundException(String jwt, Throwable throwable) {
    super("Session not found for token: " + jwt, throwable);
  }
}
