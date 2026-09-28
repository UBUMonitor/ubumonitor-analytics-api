package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import java.util.Collection;

public interface RolePersistencePort {

  void syncRoles(Collection<Role> roles);
}
