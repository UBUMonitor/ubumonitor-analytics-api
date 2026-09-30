package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.NotFoundException;

/** Indicates that a requested domain entity does not exist. */
public class EntityNotFoundException extends NotFoundException {
  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String entity, Object id) {
    super(entity + " not found with id: " + id);
  }
}
