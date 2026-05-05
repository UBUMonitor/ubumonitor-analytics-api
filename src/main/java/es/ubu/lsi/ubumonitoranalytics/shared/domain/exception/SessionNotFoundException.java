package es.ubu.lsi.ubumonitoranalytics.shared.domain.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String jwt) {
        super("Session not found for token: " + jwt);
    }
}
