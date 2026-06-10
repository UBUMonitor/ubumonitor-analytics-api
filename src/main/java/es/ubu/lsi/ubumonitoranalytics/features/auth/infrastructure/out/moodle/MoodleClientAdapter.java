package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.login.token.request.LoginTokenRequestApi;

import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.LoginAuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MoodleClientAdapter implements MoodleApiPort {

    private final Client client;
    private final MoodleAuthMapper mapper;

    @Override
    public LoginAuthResult login(AuthInput authInput) {
        LoginTokenRequestApi loginTokenRequest = mapper.toDto(authInput);
        LoginTokenResponseApi loginTokenResponse = client.login(loginTokenRequest);
        return mapper.toDomain(loginTokenResponse);
    }
}

