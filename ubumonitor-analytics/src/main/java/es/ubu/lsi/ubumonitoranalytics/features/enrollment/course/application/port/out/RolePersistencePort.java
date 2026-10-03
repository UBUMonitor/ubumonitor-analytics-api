package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import java.util.Collection;

/** Persists roles received during enrollment synchronization. */
public interface RolePersistencePort {

  /**
   * @param roles roles to synchronize
   */
  void syncRoles(Collection<Role> roles);
}
