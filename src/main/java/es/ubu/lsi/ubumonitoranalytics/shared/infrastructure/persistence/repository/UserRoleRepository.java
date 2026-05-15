package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;


import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleId>  {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserRoleEntity ur
        SET ur.active = false
        WHERE ur.id.userId IN :userIds AND ur.active = true
        """)
    void deactivateByUserIds(Iterable<Integer> userIds);

    List<UserRoleEntity> findByUserIdInAndCourseId(
        List<Integer> userIds,
        Integer courseId
    );
}
