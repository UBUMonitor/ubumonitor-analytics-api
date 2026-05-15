package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;


public interface SiteInfoPersistencePort {

    SiteInfo fetchSiteInfo(String userName);
    void save(SiteInfo site);
}

