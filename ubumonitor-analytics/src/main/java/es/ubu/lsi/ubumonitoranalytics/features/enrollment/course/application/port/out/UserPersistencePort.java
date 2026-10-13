package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import java.util.Collection;

/** Persists users received during enrollment synchronization. */
public interface UserPersistencePort {

  /**
   * @param users users to synchronize
   */
  void syncUsers(Collection<User> users);
}
