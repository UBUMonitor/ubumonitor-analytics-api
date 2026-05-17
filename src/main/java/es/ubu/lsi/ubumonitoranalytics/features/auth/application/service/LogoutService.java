package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in.LogoutUseCase;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.JooqProvider;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final SessionStorePort sessionStorePort;
    private final CurrentSessionContext currentSessionContext;
    private final JooqProvider jooqProvider;

    @Override
    public void logout() {

        SessionData sessionData = currentSessionContext.getSessionData();

        try {
            // close and remove tenant datasource associated with this session
            if (sessionData != null) {

                jooqProvider.closeTenant(sessionData);
                sessionStorePort.invalidateSession(sessionData.getJwt());
            }
        } finally {
            currentSessionContext.clear();
        }
    }
}

