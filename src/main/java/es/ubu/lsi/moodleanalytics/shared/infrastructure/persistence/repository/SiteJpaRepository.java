package es.ubu.lsi.moodleanalytics.shared.infrastructure.persistence.repository;


import es.ubu.lsi.moodleanalytics.shared.domain.entities.SiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.net.URI;
import java.util.Optional;


public interface SiteJpaRepository extends JpaRepository<SiteEntity, Long> {

    Optional<SiteEntity> findByHost(URI host);


}
