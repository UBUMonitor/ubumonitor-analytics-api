package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class InvalidDatabaseEncryptionKeyException extends RuntimeException {
    public InvalidDatabaseEncryptionKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
