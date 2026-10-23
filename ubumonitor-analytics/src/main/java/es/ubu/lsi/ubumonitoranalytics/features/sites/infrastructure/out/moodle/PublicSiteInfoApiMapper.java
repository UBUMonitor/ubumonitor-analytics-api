package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.SiteConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface PublicSiteInfoApiMapper {

  @Mapping(target = "siteConfig", source = "data")
  PublicSiteInfo toDomain(GetPublicConfigResponseApi data);

  @Mapping(target = "typeOfLogin", source = "typeoflogin")
  @Mapping(target = "enableMobileWebService", source = "enablemobilewebservice")
  @Mapping(target = "enableWebServices", source = "enablewebservices")
  @Mapping(target = "launchUrl", source = "launchurl")
  @Mapping(target = "maintenanceEnabled", source = "maintenanceenabled")
  @Mapping(target = "maintenanceMessage", source = "maintenancemessage")
  SiteConfig toDomainSiteConfig(GetPublicConfigResponseApi siteConfig);
}
