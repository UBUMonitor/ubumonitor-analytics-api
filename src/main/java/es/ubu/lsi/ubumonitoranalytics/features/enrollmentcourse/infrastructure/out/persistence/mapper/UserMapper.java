package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class)
public interface UserMapper {




    @Mapping(target = "image", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "userGroups", ignore = true)
    @Mapping(target = "userCourses", ignore = true)
    @Mapping(target = "imageData", source = "userPicture.data")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "userName", source = "userName")
    void toEntity(User user, @MappingTarget UserEntity userEntity);
}
