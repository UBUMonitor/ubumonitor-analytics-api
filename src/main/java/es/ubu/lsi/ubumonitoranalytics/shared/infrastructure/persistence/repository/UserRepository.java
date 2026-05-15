package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

}
