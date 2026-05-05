package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.persistence.SitePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class SitePersistenceAdapter implements SitePersistencePort {
    private final SiteRepository siteRepository;

    @Override
    public Integer getActualUserId(URI host, String username) {
        return siteRepository.findUserIdByHostAndUserName(host, username);
    }
}
