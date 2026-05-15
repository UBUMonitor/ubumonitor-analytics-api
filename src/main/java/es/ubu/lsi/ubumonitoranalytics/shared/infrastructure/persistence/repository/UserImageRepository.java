package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;


import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserImageRepository extends JpaRepository<UserImageEntity, Integer> {

    @Query("SELECT u.imageHash FROM UserImageEntity u WHERE u.userId = :userId")
    String findImageHashByUserId(Integer userId);



}
