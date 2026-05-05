package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.persistence;

import java.net.URI;

public interface SitePersistencePort {
    Integer getActualUserId(URI host, String username);
}
