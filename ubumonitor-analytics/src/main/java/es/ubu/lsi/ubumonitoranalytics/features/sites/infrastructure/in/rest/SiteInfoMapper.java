package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.PublicSiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInfoDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.UserInfoDto;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.LoggedUser;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoMapper {

  @Mapping(target = "user", source = "loggedUser")
  @Mapping(target = "site", source = "site")
  SiteInfoResponseDto toDto(SiteInfo siteInfo);

  PublicSiteInfoResponseDto toDto(PublicSiteInfo publicSiteInfo);

  @Mapping(target = "maintenanceMessage", source = "maintenanceMessage")
  @Mapping(target = "maintenanceEnabled", source = "maintenanceEnabled")
  @Mapping(target = "siteVersion", source = "versionNumber")
  @Mapping(target = "siteUrl", source = "host")
  SiteInfoDto toSiteInformationDto(Site site);

  UserInfoDto toUserInformationDto(LoggedUser loggedUser);
}
