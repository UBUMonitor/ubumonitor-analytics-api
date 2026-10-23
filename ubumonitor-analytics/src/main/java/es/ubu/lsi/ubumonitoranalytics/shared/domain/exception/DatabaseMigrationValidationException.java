package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class DatabaseMigrationValidationException extends RuntimeException {
  public DatabaseMigrationValidationException(String dbName, Throwable cause) {
    super("Error validating migration database: " + dbName, cause);
  }
}
