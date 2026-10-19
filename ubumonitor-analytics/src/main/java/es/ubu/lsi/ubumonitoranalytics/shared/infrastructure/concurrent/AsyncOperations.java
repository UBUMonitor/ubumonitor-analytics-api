package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Runs asynchronous work with the application's context-aware virtual-thread executor. */
@Component
@RequiredArgsConstructor
public class AsyncOperations {

  private final Executor executor;

  public <T> CompletableFuture<T> supplyAsync(Supplier<T> task) {
    return CompletableFuture.supplyAsync(task, executor);
  }

  public CompletableFuture<Void> runAsync(Runnable task) {
    return CompletableFuture.runAsync(task, executor);
  }
}
