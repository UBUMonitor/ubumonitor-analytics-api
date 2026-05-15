package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SiteEntity;
import org.mapstruct.Mapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = GlobalMapperConfig.class)
public interface SitePersistenceAdapterMapper {

    @Mapping(target = "site.id", source = "id")
    @Mapping(target = "loggedUser.id", source = "userId")
    @Mapping(target = "loggedUser.firstName", source = "firstName")
    @Mapping(target = "loggedUser.fullName", source = "fullName")
    @Mapping(target = "loggedUser.lastName", source = "lastName")
    @Mapping(target = "loggedUser.userName", source = "userName")
    @Mapping(target = "loggedUser.userPicture.url", source = "userImageUrl")
    @Mapping(target = "site.versionNumber", source = "versionNumber")
    @Mapping(target = "site.host", source = "host")
    @Mapping(target = "site.siteName", source = "siteName")
    @Mapping(target = "site.typeOfLogin", source = "typeOfLogin")
    @Mapping(target = "site.launchUrl", source = "launchUrl")
    SiteInfo toDomain(SiteEntity siteEntity);


    @Mapping(target = "id", source = "site.id")
    @Mapping(target = "versionNumber", source = "site.versionNumber")
    @Mapping(target = "host", source = "site.host")
    @Mapping(target = "siteName", source = "site.siteName")
    @Mapping(target = "typeOfLogin", source = "site.typeOfLogin")
    @Mapping(target = "launchUrl", source = "site.launchUrl")
    @Mapping(target = "userId", source = "loggedUser.id")
    @Mapping(target = "firstName", source = "loggedUser.firstName")
    @Mapping(target = "fullName", source = "loggedUser.fullName")
    @Mapping(target = "lastName", source = "loggedUser.lastName")
    @Mapping(target = "userName", source = "loggedUser.userName")
    @Mapping(target = "userImageUrl", source = "loggedUser.userPicture.url")
    void toEntity(SiteInfo siteInfo, @MappingTarget SiteEntity siteEntity);


}

