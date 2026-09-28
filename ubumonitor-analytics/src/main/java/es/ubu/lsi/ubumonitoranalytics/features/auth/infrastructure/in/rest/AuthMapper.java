package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.*;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Mapper(config = GlobalMapperConfig.class)
public interface AuthMapper {

  @Mapping(target = "cookies", ignore = true)
  @Mapping(target = "moodleToken", ignore = true)
  @Mapping(target = "dbPassword", source = "dbPassword")
  @Mapping(target = "username", source = "username")
  @Mapping(target = "password", source = "password")
  @Mapping(target = "host", source = "host")
  AuthInput toDomain(AuthLoginRequestDto authLoginRequestDto);

  @Mapping(target = "cookies", ignore = true)
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "moodleToken", source = "token")
  @Mapping(target = "dbPassword", source = "dbPassword")
  @Mapping(target = "host", source = "host")
  AuthInput toDomain(AuthTokenRequestDto authTokenRequestDto);

  @Mapping(target = "cookies", ignore = true)
  @Mapping(target = "moodleToken", ignore = true)
  @Mapping(target = "dbPassword", source = "dbPassword")
  @Mapping(target = "username", source = "username")
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "host", source = "host")
  AuthInput toDomain(AuthOfflineRequestDto authOfflineRequestDto);

  @Mapping(target = "tokenType", constant = "Bearer")
  @Mapping(target = "accessToken", source = "token")
  AuthResponseDto toAuthResponse(JwtToken jwtToken);

  @Mapping(target = "username", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "moodleToken", source = "moodleLaunchCredential", qualifiedByName = "extractMoodleToken")
  AuthInput toDomain(AuthSSORequestDto authSSORequestDto);

  @Named("extractMoodleToken")
  default String extractMoodleToken(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      URI uri = URI.create(value.trim());

      String host = uri.getAuthority();

      if (host == null || !host.startsWith("token=")) {
        return null;
      }

      // Parte Base64 después de "token="
      String base64 = host.substring("token=".length());

      // Decodificar Base64
      String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

      // Moodle:
      // md5(wwwroot + passport):::wstoken
      // o
      // md5(wwwroot + passport):::wstoken:::privatetoken
      String[] parts = decoded.split(":::", -1);

      if (parts.length < 2) {
        return null;
      }

      return parts[1];

    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
