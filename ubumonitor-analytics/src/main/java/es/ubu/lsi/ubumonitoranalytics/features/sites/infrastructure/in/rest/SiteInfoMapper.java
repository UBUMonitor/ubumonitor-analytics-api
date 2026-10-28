package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInfoDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps site information between REST DTOs and application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoMapper {

  @Mapping(target = "site", source = "site")
  SiteInfoResponseDto toDto(SiteInfo siteInfo);

  @Mapping(target = "name", source = "siteName")
  @Mapping(target = "number", source = "versionNumber")
  @Mapping(target = "url", source = "host")
  @Mapping(target = "version", source = "version")
  @Mapping(target = "release", source = "release")
  @Mapping(target = "id", source = "id")
  SiteInfoDto toSiteInformationDto(Site site);
}
