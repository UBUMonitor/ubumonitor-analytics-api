package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;


@Slf4j
@RequiredArgsConstructor
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private final CurrentSessionContext currentSessionContext;


    @Override
    public String resolveCurrentTenantIdentifier() {

        SessionData session = currentSessionContext.getSessionData();

        if (session == null) {
            return "BOOTSTRAP";
        }

        return TenantContext.buildTenantId(session);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
