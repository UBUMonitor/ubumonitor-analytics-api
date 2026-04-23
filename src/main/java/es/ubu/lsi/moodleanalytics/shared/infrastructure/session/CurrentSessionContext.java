package es.ubu.lsi.moodleanalytics.shared.infrastructure.session;

import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;

import org.springframework.stereotype.Component;

@Component
public class CurrentSessionContext {

    private static final ThreadLocal<SessionData> SESSION = new ThreadLocal<>();

    public void setSessionData(SessionData data) {
        SESSION.set(data);
    }

    public SessionData getSessionData() {
        return SESSION.get();
    }

    public void clear() {
        SESSION.remove();
    }
}
