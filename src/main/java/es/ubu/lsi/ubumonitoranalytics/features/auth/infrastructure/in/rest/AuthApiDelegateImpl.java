package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.AuthApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthLoginRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthOfflineRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthTokenRequestDto;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApiDelegateImpl implements AuthApiDelegate {

    private final AuthUseCase authUseCase;
    private final AuthMapper authMapper;


    @Override
    public AuthResponseDto authLogin(AuthLoginRequestDto loginAuthRequestDto) {
        AuthInput authInput = authMapper.toDomain(loginAuthRequestDto);
        JwtToken jwtToken = authUseCase.loginByCredentials(authInput);
        return authMapper.toAuthResponse(jwtToken);
    }

    @Override
    public AuthResponseDto authToken(AuthTokenRequestDto authTokenRequestDto) {
        AuthInput authInput = authMapper.toDomain(authTokenRequestDto);
        JwtToken jwtToken = authUseCase.loginByToken(authInput);
        return authMapper.toAuthResponse(jwtToken);
    }

    @Override
    public AuthResponseDto authOffline(AuthOfflineRequestDto authOfflineRequestDto) {
        AuthInput authInput = authMapper.toDomain(authOfflineRequestDto);
        JwtToken jwtToken = authUseCase.loginOffline(authInput);
        return authMapper.toAuthResponse(jwtToken);
    }



}
