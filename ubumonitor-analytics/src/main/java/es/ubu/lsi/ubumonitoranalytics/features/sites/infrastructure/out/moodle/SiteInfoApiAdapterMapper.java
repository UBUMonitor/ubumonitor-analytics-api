package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.LoggedUser;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.net.URI;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.util.UriComponentsBuilder;

/** Maps Moodle site responses to site application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoApiAdapterMapper {

  @Mapping(
      target = "site",
      expression = "java(toSiteInfoDomain(getSiteInfoResponseApi, getPublicConfigResponseApi))")
  @Mapping(target = "loggedUser", source = "getSiteInfoResponseApi")
  SiteInfo toDomain(
      GetSiteInfoResponseApi getSiteInfoResponseApi,
      GetPublicConfigResponseApi getPublicConfigResponseApi);

  @Mapping(target = "site", expression = "java(toSiteInfoDomain(null, getPublicConfigResponseApi))")
  PublicSiteInfo toDomain(GetPublicConfigResponseApi getPublicConfigResponseApi);

  @Mapping(target = "maintenanceEnabled", source = "getPublicConfigResponseApi.maintenanceenabled")
  @Mapping(target = "maintenanceMessage", source = "getPublicConfigResponseApi.maintenancemessage")
  @Mapping(target = "id", source = "getSiteInfoResponseApi.siteid")
  @Mapping(target = "versionNumber", source = "getSiteInfoResponseApi.version")
  @Mapping(
      target = "enableMobileWebService",
      source = "getPublicConfigResponseApi.enablemobilewebservice")
  @Mapping(target = "enableWebServices", source = "getPublicConfigResponseApi.enablewebservices")
  @Mapping(target = "host", source = "getPublicConfigResponseApi.wwwroot")
  @Mapping(target = "siteName", source = "getPublicConfigResponseApi.sitename")
  @Mapping(target = "typeOfLogin", source = "getPublicConfigResponseApi.typeoflogin")
  @Mapping(
      target = "launchUrl",
      source = "getPublicConfigResponseApi.launchurl",
      qualifiedByName = "toLaunchUrl")
  Site toSiteInfoDomain(
      GetSiteInfoResponseApi getSiteInfoResponseApi,
      GetPublicConfigResponseApi getPublicConfigResponseApi);

  @Mapping(target = "id", source = "userid")
  @Mapping(target = "userPicture.url", source = "userpictureurl")
  @Mapping(target = "userPicture.data", ignore = true)
  @Mapping(target = "username", source = "username")
  @Mapping(target = "lastName", source = "lastname")
  @Mapping(target = "fullName", source = "fullname")
  @Mapping(target = "firstName", source = "firstname")
  LoggedUser toUserDomain(GetSiteInfoResponseApi getSiteInfoResponseApi);

  @Named("toLaunchUrl")
  default URI toLaunchUrl(String launchUrl) {
    if (launchUrl == null || launchUrl.isEmpty()) {
      return null;
    }
    return UriComponentsBuilder.fromUriString(launchUrl)
        .queryParam("service", "moodle_mobile_app")
        .queryParam("urlscheme", "moodlemobile")
        .queryParam("passport", "1")
        .build()
        .toUri();
  }
}
