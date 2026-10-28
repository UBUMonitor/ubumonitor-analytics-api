package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CurrentUserInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.UserInfoDto;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface UserInfoMapper {

  @Mapping(target = "user", source = "userInfo")
  CurrentUserInfoResponseDto toDto(UserInfo userInfo);

  UserInfoDto toUserInfoDto(UserInfo userInfo);
}
