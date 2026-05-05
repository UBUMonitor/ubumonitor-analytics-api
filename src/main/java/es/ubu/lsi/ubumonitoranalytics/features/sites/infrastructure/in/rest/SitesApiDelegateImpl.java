package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.SitesApiDelegate;


import es.ubu.lsi.ubumonitoranalytics.api.generated.model.SiteInfoResponseDto;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.GetSiteInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in.SyncSiteInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SitesApiDelegateImpl implements SitesApiDelegate {

    private final GetSiteInfoUseCase getSiteInfoUseCase;
    private final SyncSiteInfoUseCase syncSiteInfoUseCase;
    private final SiteInfoMapper siteInfoMapper;

    @Override
    public SiteInfoResponseDto syncSiteInfo() {
        return siteInfoMapper.toDto(syncSiteInfoUseCase.syncSiteInfo());
    }

    @Override
    public SiteInfoResponseDto getSiteInfo() {
        return siteInfoMapper.toDto(getSiteInfoUseCase.getIteInfo());
    }



}
