package es.ubu.lsi.ubumonitoranalytics.features.system.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.system.application.port.in.ShutdownApplicationUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.application.port.out.ApplicationTerminatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ShutdownApplicationService implements ShutdownApplicationUseCase {

  private final ApplicationTerminatorPort terminator;

  @Override
  public void shutdown() {
    terminator.terminate();
  }
}
