package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sites.SITES;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SitesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SiteInfoPersistenceAdapter implements SiteInfoPersistencePort {

  private final Jooq jooq;
  private final SitePersistenceAdapterMapper mapper;

  @Override
  public void save(SiteInfo siteInfo) {

    SitesRecord sitesRecord = mapper.toRecord(siteInfo);
    sitesRecord.touched(
        SITES.ID, false); // no enviar id: que lo genere la BD / no pisar el existente

    jooq.dsl()
        .insertInto(SITES)
        .set(sitesRecord)
        .onConflict(SITES.HOST, SITES.USER_NAME)
        .doUpdate()
        .set(sitesRecord)
        .execute();
  }

  @Override
  public SiteInfo fetchSiteInfo(String username) {

    SitesRecord dto = jooq.dsl().fetchOne(SITES, SITES.USER_NAME.eq(username));

    if (dto == null) {
      throw new EntityNotFoundException("Site not found for username: " + username);
    }

    return mapper.toDomain(dto);
  }
}
