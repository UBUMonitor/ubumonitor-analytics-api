package es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.port.in.ShutdownApplicationUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.port.out.ApplicationTerminatorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class ShutdownApplicationService implements ShutdownApplicationUseCase {

  private final ApplicationTerminatorPort terminator;

  @Override
  public void shutdown() {
    terminator.terminateAfterResponse();
  }

  @Override
  public void shutdownImmediately() {
    terminator.terminate();
  }
}
