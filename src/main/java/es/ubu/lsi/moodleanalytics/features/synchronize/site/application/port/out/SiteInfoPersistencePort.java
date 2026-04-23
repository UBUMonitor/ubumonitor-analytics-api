package es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.out;

import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.Site;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;

import java.util.Optional;

public interface SiteInfoPersistencePort {
    void save(SiteInfo site);

}
