package es.ubu.lsi.moodleanalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.moodleanalytics.shared.domain.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByMoodleId(Long moodleId);
    Optional<UserEntity> findByUserName(String userName);
}
