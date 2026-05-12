package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.jwt;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.JwtUtils;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;


@Component
@RequiredArgsConstructor
public class SessionAdapter implements SessionPort {
    private final JwtUtils jwtUtils;
    private final SessionStorePort sessionStore;
    private final CurrentSessionContext currentSessionContext;
    private final MoodleConfig moodleConfig;

    @Override
    public JwtToken generateSession(AuthInput authInput) {
        String userName = authInput.getUserName();
        String dbPassword = authInput.getDbPassword();
        String moodleToken = authInput.getMoodleToken();
        URI hostUri = authInput.getHost();

        if (sessionStore.hasSession(hostUri, userName)) {
            SessionData existingSession = sessionStore.getSession(hostUri, userName);
            return toJwtToken(existingSession.getJwt());
        }

        SessionData newSession = createSession(userName, dbPassword, moodleToken, hostUri);

        return toJwtToken(newSession.getJwt());
    }


    private SessionData createSession(String userName, String dbPassword, String moodleToken, URI hostUri) {

        DatabaseUtil.createAndInitializeDatabase(moodleConfig.getDb().getBasePath(), hostUri, userName, dbPassword);

        String jwt = jwtUtils.generateJwtToken(userName, hostUri);

        SessionData sessionData = SessionData.builder()
            .jwt(jwt)
            .userName(userName)
            .host(hostUri)
            .moodleToken(moodleToken)
            .dbPassword(dbPassword)
            .build();

        currentSessionContext.setSessionData(sessionData);
        sessionStore.saveSession(jwt, sessionData);

        return sessionData;
    }

    private JwtToken toJwtToken(String jwt) {
        Jwt claims = jwtUtils.parse(jwt);
        long expiration = Duration.between(Instant.now(), claims.getExpiresAt()).getSeconds();
        return JwtToken.builder()
            .token(jwt)
            .expiresIn(expiration)
            .build();
    }


    @Override
    public boolean existDataBase(AuthInput authInput) {
        return DatabaseUtil.exists(moodleConfig.getDb().getBasePath(), authInput.getHost(), authInput.getUserName());
    }
}

