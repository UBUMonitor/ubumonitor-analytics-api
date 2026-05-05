package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;

import java.net.URI;

public interface SiteInfoPersistencePort {

    SiteInfo fetchSiteInfo(URI host, String userName);
    void save(SiteInfo site);
}
