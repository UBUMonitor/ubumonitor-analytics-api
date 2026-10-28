package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.UserImage;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersImagesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps user image records to the application image model. */
@Mapper(config = GlobalMapperConfig.class)
public interface UserImageMapper {

  @Mapping(target = "isModified", ignore = true)
  @Mapping(target = "image", source = "imageData")
  @Mapping(target = "hexHash", source = "imageHash")
  @Mapping(target = "contentType", source = "contentType")
  UserImage toDomain(UsersImagesRecord usersImagesRecord);
}
