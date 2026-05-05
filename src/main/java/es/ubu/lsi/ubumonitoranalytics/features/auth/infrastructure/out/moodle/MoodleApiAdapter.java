package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.LoginApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterLoginTokenRequestDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterLoginTokenResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.LoginAuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class MoodleApiAdapter implements MoodleApiPort {

    private final LoginApi loginApi;
    private final MoodleAuthMapper mapper;

    @Override
    public LoginAuthResult login(AuthInput authInput) {
        MoodleAdapterLoginTokenRequestDto requestDto = mapper.toDto(authInput);
        MoodleAdapterLoginTokenResponseDto responseDto = loginApi.loginToken(requestDto);
        return mapper.toDomain(responseDto);
    }
}
