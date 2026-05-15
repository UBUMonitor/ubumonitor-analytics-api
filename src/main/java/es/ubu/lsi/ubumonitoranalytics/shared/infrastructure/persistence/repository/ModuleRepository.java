package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepository extends JpaRepository<ModuleEntity,Integer> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ModuleEntity m
        SET m.active = false
        WHERE m.section.course.id IN :courseIds AND m.active = true
        """)
    void deactivateByCourseIds(Iterable<Integer> courseIds);
}
