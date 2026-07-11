package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Jooq {

  private final JooqProvider provider;
  private final CurrentSessionContext currentSessionContext;

  public @NonNull DSLContext dsl() {
    return dsl(currentSessionContext.getSessionData());
  }

  public @NonNull DSLContext dsl(SessionData sessionData) {
    return provider.getDSLContext(sessionData);
  }
}
