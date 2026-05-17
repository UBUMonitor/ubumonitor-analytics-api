package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SitesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = GlobalMapperConfig.class)
public interface SitePersistenceAdapterMapper {



    @Mapping(target = "versionNumber", source = "site.versionNumber")
    @Mapping(target = "userImageUrl", source = "loggedUser.userPicture.url")
    @Mapping(target = "typeOfLogin", source = "site.typeOfLogin")
    @Mapping(target = "siteName", source = "site.siteName")
    @Mapping(target = "launchUrl", source = "site.launchUrl")
    @Mapping(target = "id", source = "site.id")
    @Mapping(target = "host", source = "site.host")
    @Mapping(target = "userId", source = "loggedUser.id")
    @Mapping(target = "userName", source = "loggedUser.userName")
    @Mapping(target = "firstName", source = "loggedUser.firstName")
    @Mapping(target = "lastName", source = "loggedUser.lastName")
    @Mapping(target = "fullName", source = "loggedUser.fullName")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SitesRecord toRecord(SiteInfo siteInfo);

    @Mapping(target = "site.versionNumber", source = "versionNumber")
    @Mapping(target = "site.typeOfLogin", source = "typeOfLogin")
    @Mapping(target = "site.siteName", source = "siteName")
    @Mapping(target = "site.launchUrl", source = "launchUrl")
    @Mapping(target = "site.id", source = "id")
    @Mapping(target = "site.host", source = "host")
    @Mapping(target = "loggedUser.id", source = "userId")
    @Mapping(target = "loggedUser.userName", source = "userName")
    @Mapping(target = "loggedUser.firstName", source = "firstName")
    @Mapping(target = "loggedUser.lastName", source = "lastName")
    @Mapping(target = "loggedUser.fullName", source = "fullName")
    @Mapping(target = "loggedUser.userPicture.url", source = "userImageUrl")
    SiteInfo toDomain(SitesRecord dto);


}

