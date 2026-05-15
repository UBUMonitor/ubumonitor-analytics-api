package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserCourseRepository extends JpaRepository<UserCourseEntity, UserCourseId> {

    @Query("""
        SELECT uc.course
        FROM UserCourseEntity uc
        WHERE uc.user.id = :userId
        """)
    List<CourseEntity> findCoursesByUserId(Integer userId);


    @Query("""
        SELECT DISTINCT uc
        FROM UserCourseEntity uc
        JOIN FETCH uc.user u

        LEFT JOIN u.userGroups g WITH g.course.id = :courseId
        LEFT JOIN u.userRoles r WITH r.course.id = :courseId

        WHERE uc.course.id = :courseId
        """)
    List<UserCourseEntity> findByCourseWithRelations(Integer courseId);

    @Query("""
        SELECT uc FROM UserCourseEntity uc
        JOIN FETCH uc.user
        JOIN FETCH uc.course
        WHERE uc.user.id = :userId
        """)
    Optional<List<UserCourseEntity>> findByUserId(Integer userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserCourseEntity uc
        SET uc.active = false
        WHERE uc.id.courseId IN :courseIds AND uc.active = true
        """)
    int deactivateByCourseIds(Iterable<Integer> courseIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE UserCourseEntity uc
        SET uc.active = false
        WHERE uc.id.userId IN :userIds AND uc.active = true
        """)
    void deactivateByUserIds(Iterable<Integer> userIds);


    List<UserCourseEntity> findByUserIdAndActiveTrue(Integer userId);

    List<UserCourseEntity> findByUserIdInAndCourseId(List<Integer> userIds, Integer courseId);
}
