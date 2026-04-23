package es.ubu.lsi.moodleanalytics.shared.infrastructure.exception;

public class DatabaseCreationException extends RuntimeException {

    public DatabaseCreationException(String dbName, Throwable cause) {
        super("Error creating database: " + dbName, cause);
    }
}
