package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class DatabaseAlreadyExistsException extends RuntimeException {

    public DatabaseAlreadyExistsException(String name) {
        super("Database already exists: " + name);
    }
}
