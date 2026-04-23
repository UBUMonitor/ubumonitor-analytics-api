package es.ubu.lsi.moodleanalytics.shared.infrastructure.security;

import es.ubu.lsi.moodleanalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final SessionStorePort sessionStore;
    private final CurrentSessionContext currentSessionContext;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/private");
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain)
        throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token no proporcionado");
                return;
            }

            String jwt = authHeader.substring(7);

            if (!jwtUtils.isValid(jwt)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            }

            SessionData session = sessionStore.getSession(jwt);

            if (session == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión expirada o no encontrada");
                return;
            }

            // Set ThreadLocal session
            currentSessionContext.setSessionData(session);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    session.getUserId(),
                    null,
                    Collections.emptyList()
                );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } finally {
            currentSessionContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
