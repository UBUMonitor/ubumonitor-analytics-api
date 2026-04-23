package es.ubu.lsi.moodleanalytics.features.auth.application.service;

import es.ubu.lsi.moodleanalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.moodleanalytics.features.auth.application.port.out.DatabaseManagementPort;
import es.ubu.lsi.moodleanalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.LoginAuthResult;
import es.ubu.lsi.moodleanalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.moodleanalytics.shared.domain.exception.DatabaseAlreadyExistsException;
import es.ubu.lsi.moodleanalytics.shared.domain.exception.DatabaseNotExistsException;
import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.security.JwtUtils;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
import es.ubu.lsi.moodleanalytics.util.DatabaseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final DatabaseManagementPort databaseManagementPort;
    private final SessionStorePort sessionStore;
    private final JwtUtils jwtUtils;
    private final CurrentSessionContext currentSessionContext;
    private final MoodleApiPort moodleApiPort;


    @Override
    public JwtToken loginByToken(AuthInput authInput) {
        return processCommonLogin(authInput);
    }

    @Override
    public JwtToken loginByCredentials(AuthInput authInput) {
        LoginAuthResult result = moodleApiPort.login(authInput);
        authInput.setMoodleToken(result.getMoodleToken());
        return processCommonLogin(authInput);
    }

    @Override
    public JwtToken loginOffline(AuthInput authInput) {

        if(!databaseManagementPort.exists(authInput.getHost(), authInput.getUserName())) {
            throw new DatabaseNotExistsException(authInput.getHost() + "_" + authInput.getUserName());
        }
        return processCommonLogin(authInput);
    }


    private JwtToken processCommonLogin(AuthInput authInput) {
        String userName = authInput.getUserName();
        String dbPassword = authInput.getDbPassword();
        String moodleToken = authInput.getMoodleToken();
        URI hostUri = authInput.getHost();
        databaseManagementPort.createAndInitializeDatabase(hostUri, userName, dbPassword);
        String jwt = jwtUtils.generateJwtToken(userName, hostUri);

        SessionData sessionData = SessionData.builder()
            .jwt(jwt)
            .userId(userName)
            .host(hostUri)
            .moodleToken(moodleToken)
            .dbPassword(dbPassword) // Necesario para que DynamicRoutingDataSource abra el H2
            .build();

        currentSessionContext.setSessionData(sessionData);
        sessionStore.saveSession(jwt, sessionData);
        return JwtToken.builder()
            .token(jwt)
            .build();
    }


}
