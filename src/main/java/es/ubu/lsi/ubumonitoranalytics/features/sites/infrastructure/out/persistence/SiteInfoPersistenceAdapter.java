package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SitesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sites.SITES;


@Component
@RequiredArgsConstructor
public class SiteInfoPersistenceAdapter implements SiteInfoPersistencePort {

    private final Jooq jooq;
    private final SitePersistenceAdapterMapper mapper;

    @Override
    public void save(SiteInfo siteInfo) {

        SitesRecord sitesRecord = mapper.toRecord(siteInfo);

        jooq.dsl().insertInto(SITES)
            .set(sitesRecord)
            .onDuplicateKeyUpdate()
            .set(sitesRecord)
            .execute();

    }

    @Override
    public SiteInfo fetchSiteInfo(String username) {


        SitesRecord dto = jooq.dsl().fetchOne(SITES, SITES.USER_NAME.eq(username));

        if (dto == null) {
            throw new EntityNotFoundException(
                "Site not found for username: " + username
            );
        }

        return mapper.toDomain(dto);
    }
}
