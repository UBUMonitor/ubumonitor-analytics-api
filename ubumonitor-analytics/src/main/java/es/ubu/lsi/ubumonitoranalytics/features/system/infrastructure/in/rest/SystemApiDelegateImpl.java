package es.ubu.lsi.ubumonitoranalytics.features.system.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.SystemApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.features.system.application.port.in.ShutdownApplicationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SystemApiDelegateImpl implements SystemApiDelegate {

  private final ShutdownApplicationUseCase shutdownUseCase;

  @Override
  public ResponseEntity<Void> shutdown() {
    shutdownUseCase.shutdown();
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
