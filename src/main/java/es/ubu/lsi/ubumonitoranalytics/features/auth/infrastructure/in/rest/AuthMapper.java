package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthLoginRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthOfflineRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.AuthTokenRequestDto;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface AuthMapper {

    @Mapping(target = "moodleToken", ignore = true)
    @Mapping(target = "dbPassword", source = "dbPassword")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "host", source = "host")
    AuthInput toDomain(AuthLoginRequestDto authLoginRequestDto);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "moodleToken", source = "token")
    @Mapping(target = "dbPassword", source = "dbPassword")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "host", source = "host")
    AuthInput toDomain(AuthTokenRequestDto authTokenRequestDto);

    @Mapping(target = "moodleToken", ignore = true)
    @Mapping(target = "dbPassword", source = "dbPassword")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "host", source = "host")
    AuthInput toDomain(AuthOfflineRequestDto authOfflineRequestDto);



    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "accessToken", source = "token")
    AuthResponseDto toAuthResponse(JwtToken jwtToken);



}

