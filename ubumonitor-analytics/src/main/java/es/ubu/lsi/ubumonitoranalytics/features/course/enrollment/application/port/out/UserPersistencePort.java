package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import java.util.Collection;

/** Persists users received during enrollment synchronization. */
public interface UserPersistencePort {

  /**
   * @param users users to synchronize
   */
  void syncUsers(Collection<User> users);
}
