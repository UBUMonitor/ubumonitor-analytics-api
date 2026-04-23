package es.ubu.lsi.moodleanalytics.features.synchronize.site.infrastructure.in.rest;

import es.ubu.lsi.moodleanalytics.api.generated.model.SyncSiteResponseDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.SyncSiteSiteInformationDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.SyncSiteUserInformationDto;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.LoggedUser;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.Site;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncApiDelegateMapper {


    @Mapping(target = "user", source = "loggedUser")
    @Mapping(target = "site", source = "site")
    SyncSiteResponseDto toDto(SiteInfo siteInfo);


    @Mapping(target = "siteVersion", source = "versionNumber")
    @Mapping(target = "siteUrl", source = "host")
    @Mapping(target = "siteName", source = "siteName")
    @Mapping(target = "typeOfLogin", source = "typeOfLogin")
    @Mapping(target = "launchUrl", source = "launchUrl")
    SyncSiteSiteInformationDto toSiteInformationDto(Site site);

    @Mapping(target = "id", source = "moodleId")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "firstName", source = "firstName")
    SyncSiteUserInformationDto toUserInformationDto(LoggedUser loggedUser);
}
