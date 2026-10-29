package es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.port.in.ShutdownApplicationUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.infrastructure.config.ProcessMonitorProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Shuts down the application when its configured parent process exits. */
@Component
@RequiredArgsConstructor
@Slf4j
class ProcessLifecycleMonitor implements CommandLineRunner {

  private final ProcessMonitorProperties properties;
  private final ShutdownApplicationUseCase shutdownApplication;

  @Override
  public void run(String @NonNull ... args) {
    Long parentPid = properties.getParentPid();

    if (parentPid == null) {
      log.warn("No parent process PID was provided. The lifecycle monitor is disabled.");
      return;
    }

    ProcessHandle.of(parentPid)
        .ifPresentOrElse(this::monitorParentProcess, this::shutdownIfParentProcessIsUnavailable);
  }

  private void monitorParentProcess(ProcessHandle parentProcess) {
    log.info("Monitoring parent process with PID: {}", parentProcess.pid());

    parentProcess.onExit().thenRun(this::onParentProcessExit);
  }

  private void shutdownIfParentProcessIsUnavailable() {
    log.warn("The specified parent process PID is invalid or has already exited. Shutting down...");
    shutdownApplication.shutdown();
  }

  private void onParentProcessExit() {
    log.info("Parent process has exited. Shutting down the sidecar gracefully...");
    shutdownApplication.shutdown();
  }
}
