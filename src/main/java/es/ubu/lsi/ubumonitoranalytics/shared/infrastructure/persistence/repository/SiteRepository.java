package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;


import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    Optional<SiteEntity> findByHostAndUserName(URI host, String userName);

    @Query("select s.userId from SiteEntity s where s.host = :host and s.userName = :userName")
    Integer findUserIdByHostAndUserName(URI host, String userName);

}
