package es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.infrastructure.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.port.out.ApplicationTerminatorPort;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class SpringApplicationTerminator implements ApplicationTerminatorPort {

  private static final long RESPONSE_DELAY_MILLIS = 300;

  private final ConfigurableApplicationContext context;
  private final AtomicBoolean terminating = new AtomicBoolean(false);

  @Override
  public void terminate() {
    terminateWithDelay(0);
  }

  @Override
  public void terminateAfterResponse() {
    terminateWithDelay(RESPONSE_DELAY_MILLIS);
  }

  private void terminateWithDelay(long delayMillis) {
    // Idempotente: varias llamadas no lanzan varios hilos.
    if (!terminating.compareAndSet(false, true)) {
      return;
    }

    Thread.ofPlatform()
        .name("app-shutdown")
        .start(
            () -> {
              try {
                if (delayMillis > 0) {
                  try {
                    Thread.sleep(delayMillis);
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Shutdown delay interrupted. Proceeding with shutdown.");
                  }
                }

                log.info("Shutting down Spring application...");

                int exitCode = SpringApplication.exit(context, () -> 0);

                log.info("Spring application context closed. Exit code: {}", exitCode);

                System.exit(exitCode);

              } catch (Throwable error) {
                log.error("Failed to shut down Spring application gracefully.", error);
                System.exit(1);
              }
            });
  }
}
