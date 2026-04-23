package es.ubu.lsi.moodleanalytics.features.auth.infrastructure.in.rest;

import es.ubu.lsi.moodleanalytics.api.generated.api.AuthApiDelegate;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthLoginRequestDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthLoginResponseDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthOfflineRequestDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthOfflineResponseDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthTokenRequestDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthTokenResponseDto;
import es.ubu.lsi.moodleanalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.AuthInput;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApiDelegateImpl implements AuthApiDelegate {

    private final AuthUseCase authUseCase;
    private final AuthMapper authMapper;



    @Override
    public ResponseEntity<AuthLoginResponseDto> authLogin(AuthLoginRequestDto loginAuthRequestDto) {
        AuthInput authInput = authMapper.toDomain(loginAuthRequestDto);
        JwtToken jwtToken = authUseCase.loginByCredentials(authInput);
        return ResponseEntity.ok(authMapper.toAuthLoginResponseDto(jwtToken));
    }

    @Override
    public ResponseEntity<AuthTokenResponseDto> authToken(AuthTokenRequestDto authTokenRequestDto) {
        AuthInput authInput = authMapper.toDomain(authTokenRequestDto);
        JwtToken jwtToken = authUseCase.loginByToken(authInput);
        return ResponseEntity.ok(authMapper.toAuthTokenResponseDto(jwtToken));
    }

    @Override
    public ResponseEntity<AuthOfflineResponseDto> authOffline(AuthOfflineRequestDto authOfflineRequestDto) {
        AuthInput authInput = authMapper.toDomain(authOfflineRequestDto);
        JwtToken jwtToken = authUseCase.loginOffline(authInput);
        return ResponseEntity.ok(authMapper.toAuthOfflineResponseDto(jwtToken));
    }



}
