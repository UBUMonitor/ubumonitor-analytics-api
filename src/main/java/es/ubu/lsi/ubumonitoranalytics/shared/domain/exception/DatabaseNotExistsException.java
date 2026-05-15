package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class DatabaseNotExistsException extends RuntimeException {

    public DatabaseNotExistsException(String name) {
        super("Database not exists: " + name);
    }
}
