package es.ubu.lsi.moodleanalytics.features.auth.application.service;

import es.ubu.lsi.moodleanalytics.features.auth.application.port.in.LogoutUseCase;
import es.ubu.lsi.moodleanalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.database.DynamicRoutingDataSource;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final SessionStorePort sessionStorePort;
    private final CurrentSessionContext currentSessionContext;
    private final DynamicRoutingDataSource dynamicRoutingDataSource;


    @Override
    public void logout() {
        SessionData sessionData = currentSessionContext.getSessionData();
        try {
            if (sessionData != null) {
                dynamicRoutingDataSource.closeTenant(
                    sessionData.getHost(),
                    sessionData.getUserId()
                );

                sessionStorePort.invalidateSession(sessionData.getJwt());
            }
        } finally {
            currentSessionContext.clear();
        }

    }
}
