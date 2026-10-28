package es.ubu.lsi.ubumonitoranalytics.features.system.health.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.system.health.application.port.in.CheckReadinessUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.health.application.port.out.ReadinessStatusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadinessService implements CheckReadinessUseCase {

  private final ReadinessStatusPort readinessStatusPort;

  @Override
  public boolean isReady() {
    return readinessStatusPort.isAcceptingTraffic();
  }
}
