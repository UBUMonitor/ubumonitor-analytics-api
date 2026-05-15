package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = GlobalMapperConfig.class)
public interface UserGroupMapper {




    @Mapping(target = "course.id", source = "courseId")
    @Mapping(target = "user.id", source = "user.id")
    @Mapping(target = "id.userId", source = "user.id")
    @Mapping(target = "id.courseId", source = "courseId")
    UserGroupEntity toEntity(User user, Group group, Integer courseId, boolean active);
}

