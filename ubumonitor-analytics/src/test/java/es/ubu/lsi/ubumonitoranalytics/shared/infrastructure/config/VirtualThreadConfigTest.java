package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class VirtualThreadConfigTest {

  @Test
  void propagatesSessionAndClearsItOnExecutorThread() throws Exception {
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    SessionData sessionData = SessionData.builder().username("teacher").build();
    ExecutorService virtualThreadExecutor = Executors.newSingleThreadExecutor();
    Executor contextAwareExecutor =
        new VirtualThreadConfig(currentSessionContext).contextAwareExecutor(virtualThreadExecutor);

    try {
      currentSessionContext.setSessionData(sessionData);
      SessionData observed =
          CompletableFuture.supplyAsync(currentSessionContext::getSessionData, contextAwareExecutor)
              .get(5, TimeUnit.SECONDS);
      assertSame(sessionData, observed);

      assertNull(
          virtualThreadExecutor
              .submit(currentSessionContext::getSessionData)
              .get(5, TimeUnit.SECONDS));
    } finally {
      currentSessionContext.clear();
      virtualThreadExecutor.shutdownNow();
    }
  }
}
