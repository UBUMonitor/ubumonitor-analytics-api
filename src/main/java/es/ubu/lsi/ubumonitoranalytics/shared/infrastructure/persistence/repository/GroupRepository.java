package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<GroupEntity, Integer>  {
}
