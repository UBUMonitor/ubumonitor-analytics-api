package es.ubu.lsi.moodleanalytics.features.synchronize.site.infrastructure.out.moodle;


import es.ubu.lsi.moodleadapter.api.generated.api.SiteApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterSiteInfoResponseDto;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.out.SiteInfoPort;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.LoggedUser;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;
import es.ubu.lsi.moodleanalytics.shared.application.port.out.moodle.MoodleDownloaderPort;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class SiteInfoAdapter implements SiteInfoPort {

    private final SiteApi siteApi;
    private final SiteMoodleApiMapper siteMoodleApiMapper;
    private final MoodleDownloaderPort moodleDownloaderPort;
    private final CurrentSessionContext currentSessionContext;

    @Override
    public SiteInfo fetchSiteInfo() {
        MoodleAdapterSiteInfoResponseDto moodleAdapterSiteInfoResponseDto = siteApi.siteInfo();

        return siteMoodleApiMapper.toDomain(moodleAdapterSiteInfoResponseDto);



    }
}
