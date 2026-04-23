package es.ubu.lsi.moodleanalytics.shared.domain.exception;

public class DatabaseNotExistsException extends RuntimeException {

    public DatabaseNotExistsException(String name) {
        super("Database not exists: " + name);
    }
}
