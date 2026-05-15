package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import lombok.Data;

import java.net.URI;

@Data
public class Site {
    private Integer id;
    private URI host;
    private String siteName;
    private String versionNumber;
    private TypeOfLogin typeOfLogin;

    private URI launchUrl;
}

