package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence.mapper;


import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserCourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class, uses = CoursePersistenceAdapterMapper.class)
public interface EnrollmentPersistenceAdapterMapper {

    // ── Crear nueva entidad (relaciones las pone el adapter) ──
    @Mapping(target = "isFavourite", source = "isFavourite")
    @Mapping(target = "lastCourseAccess", source = "lastCourseAccess")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "active", ignore = true)
    UserCourseEntity toNewEntity(Enrollment enrollment);

    // ── Actualizar entidad existente ──
    @Mapping(target = "isFavourite", source = "isFavourite")
    @Mapping(target = "lastCourseAccess", source = "lastCourseAccess")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(Enrollment source, @MappingTarget UserCourseEntity target);

    // ── JPA → Dominio (usa CourseMapper internamente para el campo course) ──
    @Mapping(target = "course", source = "course")
    Enrollment toDomain(UserCourseEntity entity);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "enrolledCourses", source = "userCourseEntities")
    UserEnrolledCourses toDomain(Integer userId, List<UserCourseEntity> userCourseEntities);
}
