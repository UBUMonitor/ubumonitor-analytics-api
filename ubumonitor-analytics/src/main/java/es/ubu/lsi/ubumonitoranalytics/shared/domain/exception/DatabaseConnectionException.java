package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

/** Indicates that a tenant database connection could not be established. */
public class DatabaseConnectionException extends RuntimeException {

  public DatabaseConnectionException(String dbName, Throwable cause) {
    super("Error connecting to database: " + dbName, cause);
  }
}
