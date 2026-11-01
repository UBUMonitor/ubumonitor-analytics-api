package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SitesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps site persistence records to and from application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface SitePersistenceAdapterMapper {

  @Mapping(target = "userName", source = "site.userName")
  @Mapping(target = "timeZone", ignore = true)
  @Mapping(target = "version", source = "site.version")
  @Mapping(target = "release", source = "site.release")
  @Mapping(target = "versionNumber", source = "site.versionNumber")
  @Mapping(target = "siteName", source = "site.siteName")
  @Mapping(target = "id", source = "site.id")
  @Mapping(target = "host", source = "site.host")
  @Mapping(target = "userId", source = "site.userId")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  SitesRecord toRecord(SiteInfo siteInfo);

  @Mapping(target = "site", source = "dto")
  SiteInfo toDomain(SitesRecord dto);
}
