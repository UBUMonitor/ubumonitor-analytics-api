package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.LoginAuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final SessionPort sessionPort;
    private final MoodleApiPort moodleApiPort;


    @Override
    public JwtToken loginByToken(AuthInput authInput) {
        return sessionPort.generateSession(authInput);
    }

    @Override
    public JwtToken loginByCredentials(AuthInput authInput) {
        LoginAuthResult result = moodleApiPort.login(authInput);
        authInput.setMoodleToken(result.getMoodleToken());
        return sessionPort.generateSession(authInput);
    }

    @Override
    public JwtToken loginOffline(AuthInput authInput) {

        return sessionPort.generateSession(authInput);
    }

}

