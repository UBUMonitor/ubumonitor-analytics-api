

package es.ubu.lsi.moodleanalytics.features.synchronize.site.infrastructure.out.moodle;



import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterSiteInfoDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterSiteInfoResponseDto;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.LoggedUser;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.Site;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SiteMoodleApiMapper {

    @Mapping(target = "site", source = "siteInfoResponseDto")
    @Mapping(target = "loggedUser", source = "siteInfoResponseDto.siteinfo")
    SiteInfo toDomain(MoodleAdapterSiteInfoResponseDto siteInfoResponseDto);

    @Mapping(target = "host", source = "siteinfo.siteurl")
    @Mapping(target = "siteName", source = "siteinfo.sitename")
    @Mapping(target = "versionNumber", source = "siteinfo.version")
    @Mapping(target = "typeOfLogin", source = "siteconfig.typeoflogin")
    @Mapping(target = "launchUrl", source = "siteconfig.launchurl")
    Site toSiteInfoDomain(MoodleAdapterSiteInfoResponseDto siteInfoResponseDto);

    @Mapping(target = "moodleId", source = "userid")
    @Mapping(target = "userPicture.url", source = "userpictureurl")
    @Mapping(target = "userPicture.data", ignore = true)
    @Mapping(target = "userName", source = "username")
    @Mapping(target = "lastName", source = "lastname")
    @Mapping(target = "fullName", source = "fullname")
    @Mapping(target = "firstName", source = "firstname")
    LoggedUser toUserDomain(MoodleAdapterSiteInfoDto siteInfoResponseDto);


}
