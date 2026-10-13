package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ConflictException;

/** Indicates that the tenant database already exists. */
public class DatabaseAlreadyExistsException extends ConflictException {

  public DatabaseAlreadyExistsException(String name) {
    super("Database already exists: " + name);
  }
}
