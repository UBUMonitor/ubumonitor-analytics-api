package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SessionAwareJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final SessionStorePort sessionStore;
    private final CurrentSessionContext currentSessionContext;

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {


        return new JwtAuthenticationToken(jwt, Collections.emptyList(), jwt.getSubject());
    }
}

