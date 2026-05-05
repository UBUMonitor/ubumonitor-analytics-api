package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.infrastructure.out.persistence.mapper;


import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.RoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    void updateRoleEntity(Role role, @MappingTarget RoleEntity entity);


    RoleEntity createRoleEntity(Role role);
}
