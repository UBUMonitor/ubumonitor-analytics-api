package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;


import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupRepository extends JpaRepository<UserGroupEntity, UserGroupId>  {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserGroupEntity ug
        SET ug.active = false
        WHERE ug.id.userId IN :userIds AND ug.active = true
        """)
    void deactivateByUserIds(Iterable<Integer> userIds);

}
