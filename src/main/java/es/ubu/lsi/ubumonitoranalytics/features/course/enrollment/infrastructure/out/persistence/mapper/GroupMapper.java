package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.GroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class)
public interface GroupMapper {

    @Mapping(target = "id", ignore = true)
    void updateGroupEntity(Group group, @MappingTarget  GroupEntity groupEntity);

    GroupEntity createGroupEntity(Group group);
}
