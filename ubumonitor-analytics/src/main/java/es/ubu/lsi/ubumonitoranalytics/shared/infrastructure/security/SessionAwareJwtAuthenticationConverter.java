package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/** Resolves authenticated JWT principals against the active session store. */
@Component
@RequiredArgsConstructor
public class SessionAwareJwtAuthenticationConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {

    return new JwtAuthenticationToken(jwt, Collections.emptyList(), jwt.getSubject());
  }
}
