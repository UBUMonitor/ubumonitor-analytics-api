package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;


import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<SectionEntity, Integer> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE SectionEntity s
        SET s.active = false
        WHERE s.course.id IN :courseIds AND s.active = true
        """)
    void deactivateByCourseIds(Iterable<Integer> courseIds);

    @Query("""
            SELECT DISTINCT s
            FROM SectionEntity s
            LEFT JOIN FETCH s.modules
            WHERE s.course.id = :courseId
        """)
    List<SectionEntity> findByCourseIdWithModules(Integer courseId);
}
