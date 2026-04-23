package es.ubu.lsi.moodleanalytics.shared.domain.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String jwt) {
        super("Session not found for token: " + jwt);
    }
}
