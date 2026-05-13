package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.LogEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEventRepository extends JpaRepository<LogEventEntity, Short> {


}
