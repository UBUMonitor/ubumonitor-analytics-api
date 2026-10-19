package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import java.util.Collection;

/** Persists groups received during enrollment synchronization. */
public interface GroupPersistencePort {

  /**
   * @param groups groups to synchronize
   */
  void syncGroups(Collection<Group> groups);
}
