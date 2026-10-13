package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;

/** Manages lifecycle operations for tenant database resources. */
public interface TenantDatabasePort {

  /** Initializes the tenant data source and schema migration asynchronously. */
  void initializeTenantAsync(SessionData session);

  /** Closes and removes all cached tenant database resources. */
  void clearAll();

  /**
   * Closes and removes database resources associated with a tenant session.
   *
   * @param session tenant session whose database resources must be closed
   */
  void closeTenant(SessionData session);
}
