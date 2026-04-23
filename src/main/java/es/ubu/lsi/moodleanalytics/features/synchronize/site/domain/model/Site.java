package es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model;

import lombok.Data;

import java.net.URI;

@Data
public class Site {

    private URI host;
    private String siteName;
    private String versionNumber;
    private TypeOfLogin typeOfLogin;

    private URI launchUrl;
}
