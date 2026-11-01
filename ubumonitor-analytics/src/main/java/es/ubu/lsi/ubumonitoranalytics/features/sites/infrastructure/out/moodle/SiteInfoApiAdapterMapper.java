package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Maps Moodle site responses to site application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoApiAdapterMapper {

  @Mapping(target = "site", source = "getSiteInfoResponseApi")
  SiteInfo toDomain(GetSiteInfoResponseApi getSiteInfoResponseApi);

  @Mapping(target = "userName", source = "username")
  @Mapping(target = "userId", source = "userid")
  @Mapping(target = "siteName", source = "sitename")
  @Mapping(target = "host", source = "siteurl")
  @Mapping(target = "id", source = "siteid")
  @Mapping(target = "versionNumber", source = "release", qualifiedByName = "versionNumber")
  Site toSiteInfoDomain(GetSiteInfoResponseApi getSiteInfoResponseApi);

  @Named("versionNumber")
  default String getVersionNumber(String release) {
    if (release == null) {
      return null;
    }
    return release.replaceAll("^([0-9]+\\.[0-9]+).*", "$1");
  }
}
