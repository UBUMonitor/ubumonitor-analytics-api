package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TenantContext {

  public static String buildTenantId(SessionData session) {
    return session.getHost() + "_" + session.getUsername();
  }

  public static String getPassword(SessionData session) {
    return session.getDbPassword() + " " + session.getDbPassword();
  }
}
