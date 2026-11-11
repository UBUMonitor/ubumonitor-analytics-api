package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.OfflineSessionNotAllowedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/** Rejects online-only REST operations when their current session is offline. */
@Aspect
@Component
@RequiredArgsConstructor
public class RequiresOnlineSessionAspect {

  private final CurrentSessionPort currentSessionPort;

  @Around("@annotation(RequiresOnlineSession)")
  public Object requireOnlineSession(ProceedingJoinPoint joinPoint) throws Throwable {
    SessionData sessionData = currentSessionPort.getSessionData();
    if (sessionData != null && sessionData.getLoginMode() == LoginMode.OFFLINE) {
      throw new OfflineSessionNotAllowedException();
    }
    return joinPoint.proceed();
  }
}
