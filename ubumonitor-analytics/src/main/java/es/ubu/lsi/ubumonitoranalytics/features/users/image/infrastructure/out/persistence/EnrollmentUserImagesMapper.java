package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersImages.USERS_IMAGES;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.EnrollmentUserImage;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.jooq.Record2;
import org.jooq.Record4;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface EnrollmentUserImagesMapper {

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "recordValue", qualifiedByName = "extractUserId")
  @Mapping(target = "fullName", source = "recordValue", qualifiedByName = "extractFullName")
  @Mapping(target = "userImage.image", source = "recordValue", qualifiedByName = "extractImageData")
  @Mapping(
      target = "userImage.contentType",
      source = "recordValue",
      qualifiedByName = "extractContentType")
  EnrollmentUserImage toDomain(Record4<Integer, String, byte[], String> recordValue);

  @Named("extractUserId")
  default Integer extractUserId(Record4<Integer, String, byte[], String> recordValue) {
    return recordValue.get(USERS.ID);
  }

  @Named("extractFullName")
  default String extractFullName(Record4<Integer, String, byte[], String> recordValue) {
    return recordValue.get(USERS.FULL_NAME);
  }

  @Named("extractImageData")
  default byte[] extractImageData(Record4<Integer, String, byte[], String> recordValue) {
    return recordValue.get(USERS_IMAGES.IMAGE_DATA);
  }

  @Named("extractContentType")
  default String extractContentType(Record4<Integer, String, byte[], String> recordValue) {
    return recordValue.get(USERS_IMAGES.CONTENT_TYPE);
  }

  @BeanMapping(ignoreByDefault = true)
  @Mapping(target = "id", source = "recordValue", qualifiedByName = "extractUserId")
  @Mapping(target = "fullName", source = "recordValue", qualifiedByName = "extractFullName")
  @Mapping(target = "userImage.image", source = "imageData")
  @Mapping(target = "userImage.contentType", source = "contentType")
  EnrollmentUserImage toDomain(
      Record2<Integer, String> recordValue, byte[] imageData, String contentType);

  @Named("extractFullName")
  default String extractFullName(Record2<Integer, String> recordValue) {
    return recordValue.get(USERS.FULL_NAME);
  }

  @Named("extractUserId")
  default Integer extractUserId(Record2<Integer, String> recordValue) {
    return recordValue.get(USERS.ID);
  }
}
