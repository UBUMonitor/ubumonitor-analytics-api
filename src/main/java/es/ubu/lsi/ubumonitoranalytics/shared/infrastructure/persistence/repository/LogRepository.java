package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.LogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface LogRepository extends JpaRepository<LogEntity, Integer> {
    @Query("SELECT MAX(l.timestamp) FROM LogEntity l WHERE l.course.id = :courseId")
    LocalDateTime findLastTimestampByCourseId(Integer courseId);


}
