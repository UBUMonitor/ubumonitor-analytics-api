package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class DatabaseCreationException extends RuntimeException {

  public DatabaseCreationException(String dbName, Throwable cause) {
    super("Error creating database: " + dbName, cause);
  }
}
