package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.*;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Maps generated authentication DTOs to application models and responses. */
@Mapper(config = GlobalMapperConfig.class)
public interface AuthMapper {

  /** Maps username-and-password authentication input to the application model. */
  @Mapping(target = "cookies", ignore = true)
  @Mapping(target = "moodleToken", ignore = true)
  @Mapping(target = "dbPassword", source = "dbPassword")
  @Mapping(target = "username", source = "username")
  @Mapping(target = "password", source = "password")
  @Mapping(target = "host", source = "host")
  AuthInput toDomain(AuthLoginRequestDto authLoginRequestDto);

  /** Maps Moodle-token authentication input to the application model. */
  @Mapping(target = "cookies", ignore = true)
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "moodleToken", source = "token")
  @Mapping(target = "dbPassword", source = "dbPassword")
  @Mapping(target = "host", source = "host")
  AuthInput toDomain(AuthTokenRequestDto authTokenRequestDto);

  /** Maps offline authentication input to the application model. */
  @Mapping(target = "cookies", ignore = true)
  @Mapping(target = "moodleToken", ignore = true)
  @Mapping(target = "dbPassword", source = "dbPassword")
  @Mapping(target = "username", source = "username")
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "host", source = "host")
  AuthInput toDomain(AuthOfflineRequestDto authOfflineRequestDto);

  /** Maps an application JWT to the generated REST response. */
  @Mapping(target = "tokenType", constant = "Bearer")
  @Mapping(target = "accessToken", source = "token")
  AuthResponseDto toAuthResponse(JwtToken jwtToken);

  /** Maps an SSO launch credential to the application authentication model. */
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(
      target = "moodleToken",
      source = "moodleLaunchCredential",
      qualifiedByName = "extractMoodleToken")
  AuthInput toDomain(AuthSSORequestDto authSSORequestDto);

  @Named("extractMoodleToken")
  /** Extracts the Moodle web-service token from an SSO launch credential. */
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

      // Base64 payload after "token=".
      String base64 = host.substring("token=".length());

      // Decode the Base64 payload.
      String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

      // Moodle format:
      // md5(wwwroot + passport):::wstoken
      // or
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
