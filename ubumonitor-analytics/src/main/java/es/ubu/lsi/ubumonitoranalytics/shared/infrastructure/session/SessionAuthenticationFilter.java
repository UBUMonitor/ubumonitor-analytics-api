package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/** Authenticates Bearer session tokens against the in-memory session store. */
public class SessionAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final SessionStorePort sessionStore;
  private final CurrentSessionContext currentSessionContext;

  public SessionAuthenticationFilter(
      SessionStorePort sessionStore, CurrentSessionContext currentSessionContext) {
    this.sessionStore = sessionStore;
    this.currentSessionContext = currentSessionContext;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain chain)
      throws IOException, ServletException {
    String token = extractBearerToken(request);

    if (token != null) {
      SessionData session = sessionStore.getSession(token);
      if (session != null) {
        UsernamePasswordAuthenticationToken authentication =
            UsernamePasswordAuthenticationToken.authenticated(
                session.getUsername(), null, List.of());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        currentSessionContext.setSessionData(session);
      }
    }

    chain.doFilter(request, response);
  }

  private String extractBearerToken(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null
        || authorization.length() <= BEARER_PREFIX.length()
        || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
      return null;
    }

    String token = authorization.substring(BEARER_PREFIX.length());
    return token.isBlank() || token.chars().anyMatch(Character::isWhitespace) ? null : token;
  }
}
