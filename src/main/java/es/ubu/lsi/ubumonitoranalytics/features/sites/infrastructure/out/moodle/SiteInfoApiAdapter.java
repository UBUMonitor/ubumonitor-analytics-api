package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;


import es.ubu.lsi.moodleadapter.api.generated.api.SiteApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterSiteInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SiteInfoApiAdapter implements SiteInfoApiPort {

    private final SiteApi siteApi;
    private final SiteInfoApiAdapterMapper siteInfoApiAdapterMapper;

    @Override
    public SiteInfo fetchSiteInfo() {
        MoodleAdapterSiteInfoResponseDto moodleAdapterSiteInfoResponseDto = siteApi.siteInfo();

        return siteInfoApiAdapterMapper.toDomain(moodleAdapterSiteInfoResponseDto);

    }
}

