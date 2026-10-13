package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Derives tenant database identifiers and credentials from session data. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TenantContext {

  /**
   * Builds the stable tenant identifier used by the database cache.
   *
   * @param session session containing the Moodle host and username
   * @return tenant identifier
   */
  public static String buildTenantId(SessionData session) {
    return session.getHost() + "_" + session.getUsername();
  }

  /**
   * Derives the H2 password representation from the tenant session.
   *
   * @param session session containing the database encryption key
   * @return H2 password value
   */
  public static String getPassword(SessionData session) {
    return session.getDbPassword() + " " + session.getDbPassword();
  }
}
