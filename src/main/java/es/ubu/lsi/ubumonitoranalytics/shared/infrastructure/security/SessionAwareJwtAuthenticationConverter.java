package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
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
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String token = jwt.getTokenValue();

        SessionData session = sessionStore.getSession(token);
        if (session == null) {
            throw new BadCredentialsException("Sesión expirada o no encontrada");
        }

        // Set session in thread context for downstream usage
        currentSessionContext.setSessionData(session);

        return new JwtAuthenticationToken(jwt, Collections.emptyList(), jwt.getSubject());
    }
}

