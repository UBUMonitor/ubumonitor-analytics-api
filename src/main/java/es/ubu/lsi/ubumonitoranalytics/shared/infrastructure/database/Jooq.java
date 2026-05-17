package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Jooq {

    private final JooqProvider provider;
    private final CurrentSessionContext currentSessionContext;

    public DSLContext dsl() {
        return provider.getDSLContext(currentSessionContext.getSessionData());
    }
}
