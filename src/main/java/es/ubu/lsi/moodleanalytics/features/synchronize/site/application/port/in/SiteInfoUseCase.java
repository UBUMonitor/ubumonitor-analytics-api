package es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.in;

import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;

public interface SiteInfoUseCase {
    SiteInfo sync();
}
