package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.net.URI;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface EnrollmentImagesDocumentCommandMapper {
  @Mapping(target = "userIds", source = "userIds")
  @Mapping(target = "defaultImageUri", source = "defaultImageUri")
  @Mapping(target = "useDefaultPhoto", source = "useDefaultPhoto")
  @Mapping(target = "headerText", source = "headerText")
  EnrollmentImagesDocumentCommand toDomain(
      List<Long> userIds, URI defaultImageUri, Boolean useDefaultPhoto, String headerText);
}
