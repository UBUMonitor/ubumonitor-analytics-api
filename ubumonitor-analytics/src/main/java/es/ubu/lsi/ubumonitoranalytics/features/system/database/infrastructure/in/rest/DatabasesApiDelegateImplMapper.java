package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabaseHostInfoDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabaseUserInfoDto;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseHostInfo;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface DatabasesApiDelegateImplMapper {

  List<DatabaseHostInfoDto> toDatabaseHostInfoDto(List<DatabaseHostInfo> databasesInfo);

  @Mapping(target = "location", source = "databaseFile")
  DatabaseUserInfoDto toDatabaseUserInfoDto(DatabaseInfo databaseInfo);

  @Mapping(target = "moodleUrl", source = "host")
  @Mapping(target = "users", source = "databases")
  DatabaseHostInfoDto toDatabaseHostInfoDto(DatabaseHostInfo databaseHostInfo);
}
