package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface EnrollmentImagesDocumentCommandMapper {
  @Mapping(target = "userIds", source = "userIds")
  @Mapping(target = "locale", source = "locale")
  @Mapping(target = "defaultImagePath", source = "defaultImagePath")
  @Mapping(target = "useDefaultPhoto", source = "useDefaultPhoto")
  @Mapping(
      target = "headerText",
      source = "headerText",
      defaultValue = "UBUMonitor Analytics - Course Users Enrollments")
  EnrollmentImagesDocumentCommand toDomain(
      List<Integer> userIds,
      Locale locale,
      String defaultImagePath,
      Boolean useDefaultPhoto,
      String headerText);
}
