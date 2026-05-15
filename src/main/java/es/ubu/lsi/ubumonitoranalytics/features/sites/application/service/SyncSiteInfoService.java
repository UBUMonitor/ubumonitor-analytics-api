package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.SyncSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncSiteInfoService implements SyncSiteInfoUseCase {

    private final SiteInfoApiPort siteInfoApiPort;
    private final SiteInfoPersistencePort siteInfoPersistencePort;


    @Override
    public SiteInfo syncSiteInfo() {
        SiteInfo siteInfo = siteInfoApiPort.fetchSiteInfo();
        siteInfoPersistencePort.save(siteInfo);
        return siteInfo;
    }
}

