package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

/** Indicates that the supplied key cannot open the encrypted tenant database. */
public class InvalidDatabaseEncryptionKeyException extends RuntimeException {
  public InvalidDatabaseEncryptionKeyException(String message, Throwable cause) {
    super(message, cause);
  }
}
