package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class)
public interface UserMapper {




    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "userGroups", ignore = true)
    @Mapping(target = "userCourses", ignore = true)
    @Mapping(target = "image.imageHash", source = "userPicture.hexHash")
    @Mapping(target = "image.imageData", source = "userPicture.data")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "lastName", source = "lastName")
    void toEntity(User user, @MappingTarget UserEntity userEntity);

    @AfterMapping
    default void linkUser(@MappingTarget UserEntity userEntity, User user) {
        if (userEntity.getFirstName() != null) {
            userEntity.getImage().setUser(userEntity);
        }
    }
}
