package es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.out;

import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;

public interface SiteInfoPort {

    SiteInfo fetchSiteInfo();
}
