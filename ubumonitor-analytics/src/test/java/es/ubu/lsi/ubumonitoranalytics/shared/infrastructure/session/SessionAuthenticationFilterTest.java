package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class SessionAuthenticationFilterTest {

  private final SessionStorePort sessionStore = mock(SessionStorePort.class);
  private final CurrentSessionContext currentSessionContext = new CurrentSessionContext();
  private final SessionAuthenticationFilter filter =
      new SessionAuthenticationFilter(sessionStore, currentSessionContext);

  @Test
  void authenticatesBearerTokenFoundInSessionStore() throws Exception {
    SessionData session = SessionData.builder().username("teacher").build();
    when(sessionStore.getSession("opaque-session-token")).thenReturn(session);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer opaque-session-token");

    filter.doFilter(
        request,
        new MockHttpServletResponse(),
        (requestInChain, responseInChain) ->
            assertSame(session, currentSessionContext.getSessionData()));

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertTrue(authentication.isAuthenticated());
    assertEquals("teacher", authentication.getPrincipal());
    assertNull(currentSessionContext.getSessionData());
    SecurityContextHolder.clearContext();
  }

  @Test
  void leavesRequestUnauthenticatedWhenTokenIsUnknown() throws Exception {
    when(sessionStore.getSession("unknown-token")).thenReturn(null);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer unknown-token");

    filter.doFilter(
        request,
        new MockHttpServletResponse(),
        (requestInChain, responseInChain) -> assertNull(currentSessionContext.getSessionData()));

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    assertNull(currentSessionContext.getSessionData());
    SecurityContextHolder.clearContext();
  }

  @Test
  void propagatesRuntimeExceptionAndEndsSessionScope() {
    RuntimeException expected = new IllegalStateException("request failed");

    RuntimeException actual =
        assertThrows(
            RuntimeException.class,
            () ->
                currentSessionContext.runInRequestScope(
                    null,
                    () -> {
                      throw expected;
                    }));

    assertSame(expected, actual);
    assertNull(currentSessionContext.getSessionData());
  }
}
