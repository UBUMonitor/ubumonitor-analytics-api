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
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;



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
        Claims claims = jwtUtils.parse(jwt);
        long expiration = claims.getExpiration().getTime() - System.currentTimeMillis();
        return JwtToken.builder()
            .token(jwt)
            .expiresIn(expiration / 1000)
            .build();
    }

    private boolean isSameSession(SessionData session, String userName, String dbPassword) {
        return session != null
            && userName.equals(session.getUserName())
            && dbPassword.equals(session.getDbPassword());
    }


    @Override
    public boolean existDataBase(AuthInput authInput) {
        return DatabaseUtil.exists(moodleConfig.getDb().getBasePath(), authInput.getHost(), authInput.getUserName());
    }
}

