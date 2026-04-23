package es.ubu.lsi.moodleanalytics.shared.infrastructure.exception;

public class DatabaseConnectionException extends RuntimeException {

    public DatabaseConnectionException(String dbName, Throwable cause) {
        super("Error connecting to database: " + dbName, cause);
    }
}
