package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class DatabaseConnectionException extends RuntimeException {

  public DatabaseConnectionException(String dbName, Throwable cause) {
    super("Error connecting to database: " + dbName, cause);
  }
}
