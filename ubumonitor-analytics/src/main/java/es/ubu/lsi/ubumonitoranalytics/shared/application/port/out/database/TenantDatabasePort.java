package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;

/** Manages lifecycle operations for tenant database resources. */
public interface TenantDatabasePort {

  /**
   * Closes and removes database resources associated with a tenant session.
   *
   * @param session tenant session whose database resources must be closed
   */
  void closeTenant(SessionData session);
}
