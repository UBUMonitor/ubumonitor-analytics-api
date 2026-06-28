package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.config;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class VirtualThreadConfig {

  private final CurrentSessionContext sessionContext;

  @Bean
  public ExecutorService virtualThreadExecutor() {
    return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
  }

  @Bean
  @Primary
  public Executor contextAwareExecutor(ExecutorService virtualThreadExecutor) {

    return command -> {
      SessionData captured = sessionContext.getSessionData();

      virtualThreadExecutor.execute(
          () -> {
            if (captured != null) {
              sessionContext.setSessionData(captured);
            }

            try {
              command.run();
            } finally {
              sessionContext.clear();
            }
          });
    };
  }
}
