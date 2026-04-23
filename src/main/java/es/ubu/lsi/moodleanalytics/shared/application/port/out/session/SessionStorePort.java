package es.ubu.lsi.moodleanalytics.shared.application.port.out.session;

import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;

public interface SessionStorePort {
    void saveSession(String jwt, SessionData sessionData);
    SessionData getSession(String jwt);
    void invalidateSession(String jwt);
}
