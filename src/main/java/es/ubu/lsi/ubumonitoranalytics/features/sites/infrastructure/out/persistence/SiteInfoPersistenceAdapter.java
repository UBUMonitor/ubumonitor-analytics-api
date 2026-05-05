package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SiteEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class SiteInfoPersistenceAdapter implements SiteInfoPersistencePort {

    private final SiteRepository siteRepository;
    private final SitePersistenceAdapterMapper sitePersistenceAdapterMapper;


    @Override
    @Transactional
    public void save(SiteInfo siteInfo) {
        SiteEntity siteEntity = siteRepository
            .findById(siteInfo.getSite().getId())
            .orElseGet(SiteEntity::new);

        sitePersistenceAdapterMapper.toEntity(siteInfo, siteEntity);

        siteRepository.save(siteEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public SiteInfo fetchSiteInfo(URI host, String userName) {
        return siteRepository.findByHostAndUserName(host, userName)
            .map(sitePersistenceAdapterMapper::toDomain)
            .orElseThrow(() -> new EntityNotFoundException("Site not found for host: " + host));
    }
}
