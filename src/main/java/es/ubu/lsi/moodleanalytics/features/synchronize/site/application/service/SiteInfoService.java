package es.ubu.lsi.moodleanalytics.features.synchronize.site.application.service;

import es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.in.SiteInfoUseCase;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.out.SiteInfoPort;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.LoggedUser;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;
import es.ubu.lsi.moodleanalytics.shared.application.port.out.moodle.MoodleDownloaderPort;
import es.ubu.lsi.moodleanalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SiteInfoService implements SiteInfoUseCase {

    private final SiteInfoPort siteInfoPort;
    private final SiteInfoPersistencePort siteInfoPersistencePort;


    @Override
    public SiteInfo sync() {
        SiteInfo siteInfo = siteInfoPort.fetchSiteInfo();

        siteInfoPersistencePort.save(siteInfo);

        return siteInfo;
    }
}
