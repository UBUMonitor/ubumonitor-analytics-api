package es.ubu.lsi.ubumonitoranalytics.features.health.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.HealthApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.features.health.application.port.in.CheckReadinessUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** Implements the generated REST operations for Moodle course grade information. */
@Service
@RequiredArgsConstructor
public class HealthApiDelegateImpl implements HealthApiDelegate {

  private final CheckReadinessUseCase checkReadinessUseCase;

  @Override
  public ResponseEntity<Void> checkHealth() {
    if (checkReadinessUseCase.isReady()) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.status(503).build();
  }
}
