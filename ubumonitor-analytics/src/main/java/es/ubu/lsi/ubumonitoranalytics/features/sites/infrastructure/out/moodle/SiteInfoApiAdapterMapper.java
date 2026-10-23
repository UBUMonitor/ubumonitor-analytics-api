package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.LoggedUser;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.MapperUtils;
import java.time.ZoneId;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Maps Moodle site responses to site application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoApiAdapterMapper {

  @Mapping(target = "site", source = "getSiteInfoResponseApi")
  @Mapping(target = "loggedUser", source = "getSiteInfoResponseApi")
  SiteInfo toDomain(GetSiteInfoResponseApi getSiteInfoResponseApi, @Context ZoneId timeZone);

  @Mapping(target = "siteName", source = "getSiteInfoResponseApi.sitename")
  @Mapping(target = "host", source = "getSiteInfoResponseApi.siteurl")
  @Mapping(target = "id", source = "getSiteInfoResponseApi.siteid")
  @Mapping(target = "versionNumber", source = "getSiteInfoResponseApi.version")
  Site toSiteInfoDomain(GetSiteInfoResponseApi getSiteInfoResponseApi);

  @Mapping(
      target = "timeZone",
      source = "getSiteInfoResponseApi",
      qualifiedByName = "zoneIdToString")
  @Mapping(target = "id", source = "userid")
  @Mapping(target = "userPicture.url", source = "userpictureurl")
  @Mapping(target = "userPicture.data", ignore = true)
  @Mapping(target = "username", source = "username")
  @Mapping(target = "lastName", source = "lastname")
  @Mapping(target = "fullName", source = "fullname")
  @Mapping(target = "firstName", source = "firstname")
  LoggedUser toUserDomain(GetSiteInfoResponseApi getSiteInfoResponseApi, @Context ZoneId zoneId);

  @Named("zoneIdToString")
  default String zoneIdToString(
      GetSiteInfoResponseApi getSiteInfoResponseApi, @Context ZoneId zoneId) {
    return MapperUtils.toZoneIdString(zoneId);
  }
}
