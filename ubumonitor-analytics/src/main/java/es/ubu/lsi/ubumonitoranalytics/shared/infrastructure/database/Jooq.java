package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/** Provides a jOOQ context bound to the current tenant session. */
@Component
@RequiredArgsConstructor
public class Jooq {

  private final JooqProvider provider;
  private final CurrentSessionContext currentSessionContext;

  /** Returns a jOOQ context for the tenant associated with the current request. */
  public @NonNull DSLContext dsl() {
    return dsl(currentSessionContext.getSessionData());
  }

  /**
   * Returns a jOOQ context for the tenant represented by the supplied session.
   *
   * @param sessionData tenant session used to resolve the database
   * @return tenant-specific jOOQ context
   */
  public @NonNull DSLContext dsl(SessionData sessionData) {
    return provider.getDSLContext(sessionData);
  }
}
