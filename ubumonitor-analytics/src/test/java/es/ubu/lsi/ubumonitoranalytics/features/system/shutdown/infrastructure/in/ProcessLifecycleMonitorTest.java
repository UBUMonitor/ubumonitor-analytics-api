package es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.infrastructure.in;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.port.in.ShutdownApplicationUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.infrastructure.config.ProcessMonitorProperties;
import org.junit.jupiter.api.Test;

class ProcessLifecycleMonitorTest {

  @Test
  void shouldNotMonitorWhenParentPidIsNotConfigured() {
    ProcessMonitorProperties properties = new ProcessMonitorProperties();
    ShutdownApplicationUseCase shutdownApplication = mock(ShutdownApplicationUseCase.class);

    new ProcessLifecycleMonitor(properties, shutdownApplication).run();

    verifyNoInteractions(shutdownApplication);
  }

  @Test
  void shouldShutDownImmediatelyWhenParentProcessIsUnavailable() {
    ProcessMonitorProperties properties = new ProcessMonitorProperties();
    properties.setParentPid(Long.MAX_VALUE);
    ShutdownApplicationUseCase shutdownApplication = mock(ShutdownApplicationUseCase.class);

    new ProcessLifecycleMonitor(properties, shutdownApplication).run();

    verify(shutdownApplication).shutdownImmediately();
  }
}
