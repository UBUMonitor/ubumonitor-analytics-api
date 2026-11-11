package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.OfflineSessionNotAllowedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

class RequiresOnlineSessionAspectTest {

  private final CurrentSessionPort currentSessionContext = mock(CurrentSessionPort.class);
  private final RequiresOnlineSessionAspect aspect =
      new RequiresOnlineSessionAspect(currentSessionContext);
  private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

  @Test
  void rejectsOfflineSessionWithoutProceeding() throws Throwable {
    when(currentSessionContext.getSessionData())
        .thenReturn(SessionData.builder().loginMode(LoginMode.OFFLINE).build());

    assertThrows(
        OfflineSessionNotAllowedException.class, () -> aspect.requireOnlineSession(joinPoint));

    verify(joinPoint, never()).proceed();
  }

  @Test
  void allowsOnlineSession() throws Throwable {
    when(currentSessionContext.getSessionData())
        .thenReturn(SessionData.builder().loginMode(LoginMode.CREDENTIALS).build());
    when(joinPoint.proceed()).thenReturn("result");

    assertEquals("result", aspect.requireOnlineSession(joinPoint));

    verify(joinPoint).proceed();
  }

  @Test
  void allowsMissingSession() throws Throwable {
    when(currentSessionContext.getSessionData()).thenReturn(null);
    when(joinPoint.proceed()).thenReturn("result");

    assertEquals("result", aspect.requireOnlineSession(joinPoint));

    verify(joinPoint).proceed();
  }
}
