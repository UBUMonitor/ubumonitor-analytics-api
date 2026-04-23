package es.ubu.lsi.moodleanalytics.features.synchronize.site.infrastructure.out.persistence;


import es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.Site;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.infrastructure.out.persistence.mapper.SitePersistenceAdapterMapper;
import es.ubu.lsi.moodleanalytics.shared.domain.entities.SiteEntity;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.persistence.repository.SiteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SiteInfoPersistenceAdapter implements SiteInfoPersistencePort {
    private final SiteJpaRepository siteJpaRepository;
    private final SitePersistenceAdapterMapper sitePersistenceAdapterMapper;


    @Override
    public void save(SiteInfo siteInfo) {
        SiteEntity siteEntity = siteJpaRepository.findByHost(siteInfo.getSite().getHost())
            .map( entity -> sitePersistenceAdapterMapper.toEntity(siteInfo, entity))
            .orElseGet(() -> sitePersistenceAdapterMapper.toEntity(siteInfo, new SiteEntity()));

        siteJpaRepository.save(siteEntity);
    }
}
