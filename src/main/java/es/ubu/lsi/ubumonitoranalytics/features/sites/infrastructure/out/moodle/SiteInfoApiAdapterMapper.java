package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.moodle.model.tool.mobile.getpublicconfig.response.GetPublicConfigResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.LoggedUser;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoApiAdapterMapper {

  @Mapping(
      target = "site",
      expression = "java(toSiteInfoDomain(getSiteInfoResponseApi, getPublicConfigResponseApi))")
  @Mapping(target = "loggedUser", source = "getSiteInfoResponseApi")
  SiteInfo toDomain(
      GetSiteInfoResponseApi getSiteInfoResponseApi,
      GetPublicConfigResponseApi getPublicConfigResponseApi);

  @Mapping(target = "id", source = "getSiteInfoResponseApi.siteid")
  @Mapping(target = "host", source = "getSiteInfoResponseApi.siteurl")
  @Mapping(target = "siteName", source = "getSiteInfoResponseApi.sitename")
  @Mapping(target = "versionNumber", source = "getSiteInfoResponseApi.version")
  @Mapping(target = "typeOfLogin", source = "getPublicConfigResponseApi.typeoflogin")
  @Mapping(target = "launchUrl", source = "getPublicConfigResponseApi.launchurl")
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
}
