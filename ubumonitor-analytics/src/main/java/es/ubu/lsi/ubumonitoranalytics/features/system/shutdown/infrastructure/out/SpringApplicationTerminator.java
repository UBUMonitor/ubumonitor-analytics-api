package es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.infrastructure.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.port.out.ApplicationTerminatorPort;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
    // Idempotente: varias llamadas no lanzan varios hilos
    if (!terminating.compareAndSet(false, true)) {
      return;
    }
    Thread.ofPlatform()
        .name("app-shutdown")
        .start(
            () -> {
              if (delayMillis > 0) {
                try {
                  Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                }
              }
              int code = SpringApplication.exit(context, () -> 0);
              System.exit(code);
            });
  }
}
