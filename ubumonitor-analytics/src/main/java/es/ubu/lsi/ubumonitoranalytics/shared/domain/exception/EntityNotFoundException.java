package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.NotFoundException;

public class EntityNotFoundException extends NotFoundException {
  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String entity, Object id) {
    super(entity + " not found with id: " + id);
  }
}
