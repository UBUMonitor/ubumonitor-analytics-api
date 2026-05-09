package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface UserGroupMapper {



    default List<UserGroupEntity> toUserGroupoEntities(User user, List<Group> groups, Integer courseId, boolean active) {
        if (groups == null) {
            return List.of();
        }
        return groups.stream()
            .map( group -> toUserRoleEntity(user, group, courseId, active))
            .toList();
    }

    @Mapping(target = "course", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.courseId", source = "courseId")
    UserGroupEntity toUserRoleEntity(User user, Group group, Integer courseId, boolean active);
}
