package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException() {
        super();
    }
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entity, Object id) {
        super(entity + " not found with id: " + id);
    }
}
