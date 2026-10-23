package es.ubu.lsi.ubumonitoranalytics.features.system.infrastructure.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.application.port.out.ApplicationTerminatorPort;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SpringApplicationTerminator implements ApplicationTerminatorPort {

  private final ConfigurableApplicationContext context;
  private final AtomicBoolean terminating = new AtomicBoolean(false);

  @Override
  public void terminate() {
    // Idempotente: varias llamadas no lanzan varios hilos
    if (!terminating.compareAndSet(false, true)) {
      return;
    }
    Thread.ofPlatform()
        .name("app-shutdown")
        .start(
            () -> {
              try {
                Thread.sleep(300); // deja que se envíe el 202
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              int code = SpringApplication.exit(context, () -> 0);
              System.exit(code);
            });
  }
}
