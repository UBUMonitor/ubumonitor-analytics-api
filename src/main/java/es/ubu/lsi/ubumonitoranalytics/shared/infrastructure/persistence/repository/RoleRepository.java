package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.GroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer>  {
}
