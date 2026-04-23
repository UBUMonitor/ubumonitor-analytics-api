package es.ubu.lsi.moodleanalytics.features.synchronize.site.infrastructure.out.persistence.mapper;

import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.Site;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;
import es.ubu.lsi.moodleanalytics.shared.domain.entities.SiteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SitePersistenceAdapterMapper {



    @Mapping(target = "id", ignore = true)
    @Mapping(target = "moodleId", source = "loggedUser.moodleId")
    @Mapping(target = "firstName", source = "loggedUser.firstName")
    @Mapping(target = "fullName", source = "loggedUser.fullName")
    @Mapping(target = "lastName", source = "loggedUser.lastName")
    @Mapping(target = "userName", source = "loggedUser.userName")
    @Mapping(target = "userImageUrl", source = "loggedUser.userPicture.url")
    @Mapping(target = "versionNumber", source = "site.versionNumber")
    @Mapping(target = "host", source = "site.host")
    @Mapping(target = "siteName", source = "site.siteName")
    @Mapping(target = "typeOfLogin", source = "site.typeOfLogin")
    @Mapping(target = "launchUrl", source = "site.launchUrl")
    SiteEntity toEntity(SiteInfo siteInfo, @MappingTarget SiteEntity siteEntity);
}
