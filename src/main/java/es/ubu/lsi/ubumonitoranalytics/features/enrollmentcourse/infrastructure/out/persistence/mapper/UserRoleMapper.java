package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface UserRoleMapper {



    default List<UserRoleEntity> toUserRolesEntities(User user, List<Role> roles, Integer courseId, boolean active) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
            .map( role -> toUserRoleEntity(user, role, courseId, active))
            .toList();
    }

    @Mapping(target = "course.id", source = "courseId")
    @Mapping(target = "user.id", source = "user.id")
    @Mapping(target = "role.id", source = "role.id")
    @Mapping(target = "id.roleId", source = "role.id")
    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.courseId", source = "courseId")
    UserRoleEntity toUserRoleEntity(User user, Role role, Integer courseId, boolean active);
}
