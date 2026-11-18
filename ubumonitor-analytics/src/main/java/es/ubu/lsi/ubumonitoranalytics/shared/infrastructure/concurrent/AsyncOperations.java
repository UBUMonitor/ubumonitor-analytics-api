package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Runs asynchronous work with the application's context-aware virtual-thread executor. */
@Component
@RequiredArgsConstructor
public class AsyncOperations implements AsyncExecutionPort {

  private final Executor executor;

  @Override
  public <T> CompletableFuture<T> supplyAsync(Supplier<T> task) {
    return CompletableFuture.supplyAsync(task, executor);
  }

  @Override
  public CompletableFuture<Void> runAsync(Runnable task) {
    return CompletableFuture.runAsync(task, executor);
  }
}
