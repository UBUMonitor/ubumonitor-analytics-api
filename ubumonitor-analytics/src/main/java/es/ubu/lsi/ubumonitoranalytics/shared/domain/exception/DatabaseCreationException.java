package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

/** Indicates that a tenant database could not be created or initialized. */
public class DatabaseCreationException extends RuntimeException {

  public DatabaseCreationException(String dbName, Throwable cause) {
    super("Error creating database: " + dbName, cause);
  }
}
