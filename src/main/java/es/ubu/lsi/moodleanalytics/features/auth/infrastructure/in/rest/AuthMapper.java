package es.ubu.lsi.moodleanalytics.features.auth.infrastructure.in.rest;

import es.ubu.lsi.moodleanalytics.api.generated.model.AuthLoginRequestDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthLoginResponseDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthOfflineRequestDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthOfflineResponseDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthTokenRequestDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.AuthTokenResponseDto;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.AuthInput;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.net.IDN;
import java.net.URI;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "moodleToken", ignore = true)
    @Mapping(target = "dbPassword", source = "dbPassword")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "host", source = "host")
    AuthInput toDomain(AuthLoginRequestDto authLoginRequestDto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "moodleToken", source = "token")
    @Mapping(target = "dbPassword", source = "dbPassword")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "host", source = "host")
    AuthInput toDomain(AuthTokenRequestDto authTokenRequestDto);

    @Mapping(target = "moodleToken", ignore = true)
    @Mapping(target = "dbPassword", source = "dbPassword")
    @Mapping(target = "userName", source = "userName")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "host", source = "host")
    AuthInput toDomain(AuthOfflineRequestDto authOfflineRequestDto);



    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "accessToken", source = "token")
    AuthLoginResponseDto toAuthLoginResponseDto(JwtToken jwtToken);


    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "accessToken", source = "token")
    AuthOfflineResponseDto toAuthOfflineResponseDto(JwtToken jwtToken);

    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "accessToken", source = "token")
    AuthTokenResponseDto toAuthTokenResponseDto(JwtToken jwtToken);

}
