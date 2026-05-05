
package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.in.rest;


import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInformationDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.UserInformationDto;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.LoggedUser;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import org.mapstruct.Mapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoMapper {

    @Mapping(target = "user", source = "loggedUser")
    @Mapping(target = "site", source = "site")
    SiteInfoResponseDto toDto(SiteInfo siteInfo);

    @Mapping(target = "siteVersion", source = "versionNumber")
    @Mapping(target = "siteUrl", source = "host")
    SiteInformationDto toSiteInformationDto(Site site);

    UserInformationDto toUserInformationDto(LoggedUser loggedUser);


}
