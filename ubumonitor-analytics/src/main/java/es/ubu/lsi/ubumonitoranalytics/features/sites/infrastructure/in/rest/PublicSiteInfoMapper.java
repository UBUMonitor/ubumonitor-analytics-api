package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.PublicSiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface PublicSiteInfoMapper {

  @Mapping(target = "siteConfig", source = "siteConfig")
  PublicSiteInfoResponseDto toDto(PublicSiteInfo publicSiteInfo);
}
