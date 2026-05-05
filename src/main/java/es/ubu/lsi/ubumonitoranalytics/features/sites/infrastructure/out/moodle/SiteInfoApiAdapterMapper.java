

package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;


import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterSiteInfoDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterSiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.LoggedUser;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;

import org.mapstruct.Mapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface SiteInfoApiAdapterMapper {

    @Mapping(target = "site", source = "siteInfoResponseDto")
    @Mapping(target = "loggedUser", source = "siteInfoResponseDto.siteinfo")
    SiteInfo toDomain(MoodleAdapterSiteInfoResponseDto siteInfoResponseDto);

    @Mapping(target = "id", source = "siteinfo.siteid")
    @Mapping(target = "host", source = "siteinfo.siteurl")
    @Mapping(target = "siteName", source = "siteinfo.sitename")
    @Mapping(target = "versionNumber", source = "siteinfo.version")
    @Mapping(target = "typeOfLogin", source = "siteconfig.typeoflogin")
    @Mapping(target = "launchUrl", source = "siteconfig.launchurl")
    Site toSiteInfoDomain(MoodleAdapterSiteInfoResponseDto siteInfoResponseDto);

    @Mapping(target = "id", source = "userid")
    @Mapping(target = "userPicture.url", source = "userpictureurl")
    @Mapping(target = "userPicture.data", ignore = true)
    @Mapping(target = "userName", source = "username")
    @Mapping(target = "lastName", source = "lastname")
    @Mapping(target = "fullName", source = "fullname")
    @Mapping(target = "firstName", source = "firstname")
    LoggedUser toUserDomain(MoodleAdapterSiteInfoDto siteInfoResponseDto);



}
