package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ActualUserRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface UserInfoPersistenceMapper {
  @Mapping(target = "isServerTimeZone", source = "serverTimeZone")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", source = "userInfo", qualifiedByName = "now")
  ActualUserRecord toRecord(UserInfo userInfo);

  @Mapping(target = "serverTimeZone", source = "isServerTimeZone")
  UserInfo toDomain(ActualUserRecord actualUserRecord);
}
