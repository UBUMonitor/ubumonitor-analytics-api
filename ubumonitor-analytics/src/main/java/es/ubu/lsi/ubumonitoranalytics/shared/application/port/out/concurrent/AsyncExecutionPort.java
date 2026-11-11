package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** Executes tasks asynchronously using the application's configured execution context. */
public interface AsyncExecutionPort {

  <T> CompletableFuture<T> supplyAsync(Supplier<T> task);

  CompletableFuture<Void> runAsync(Runnable task);
}
