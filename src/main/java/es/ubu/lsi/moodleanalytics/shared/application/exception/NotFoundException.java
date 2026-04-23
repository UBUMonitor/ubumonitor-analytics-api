package es.ubu.lsi.moodleanalytics.shared.application.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
