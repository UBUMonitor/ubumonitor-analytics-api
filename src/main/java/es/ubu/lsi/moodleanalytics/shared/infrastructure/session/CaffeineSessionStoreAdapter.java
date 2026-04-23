package es.ubu.lsi.moodleanalytics.shared.infrastructure.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.ubu.lsi.moodleanalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.database.DynamicRoutingDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CaffeineSessionStoreAdapter implements SessionStorePort {

    private final Cache<String, SessionData> sessionCache = Caffeine.newBuilder()
        .expireAfterAccess(24, TimeUnit.HOURS)
        .maximumSize(1000)
        .build();


    @Override
    public void saveSession(String jwt, SessionData sessionData) {
        sessionCache.put(jwt, sessionData);
    }

    @Override
    public SessionData getSession(String jwt) {
        return sessionCache.getIfPresent(jwt);
    }

    @Override
    public void invalidateSession(String jwt) {
        sessionCache.invalidate(jwt);

    }
}
