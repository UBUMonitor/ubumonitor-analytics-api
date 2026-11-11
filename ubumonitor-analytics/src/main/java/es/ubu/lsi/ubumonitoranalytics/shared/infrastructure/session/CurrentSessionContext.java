package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/** Exposes the session data associated with the current request. */
@Component
public class CurrentSessionContext {

  private static final ScopedValue<Optional<SessionData>> SESSION = ScopedValue.newInstance();

  /** Returns the current session data, or {@code null} when no session is set. */
  public SessionData getSessionData() {
    return SESSION.isBound() ? SESSION.get().orElse(null) : null;
  }

  /** Runs a servlet request within a fresh session scope. */
  public void runInRequestScope(SessionData data, RequestAction action)
      throws IOException, ServletException {
    try {
      ScopedValue.where(SESSION, Optional.ofNullable(data))
          .call(
              () -> {
                action.run();
                return null;
              });
    } catch (IOException | ServletException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IllegalStateException("Unexpected exception in session scope.", exception);
    }
  }

  /** Runs a task within a fresh session scope. */
  public void runWithSessionData(SessionData data, Runnable action) {
    ScopedValue.where(SESSION, Optional.ofNullable(data)).run(action);
  }

  /** Returns the result of a task run within a fresh session scope. */
  public <T> T callWithSessionData(SessionData data, Supplier<T> action) {
    try {
      return ScopedValue.where(SESSION, Optional.ofNullable(data)).call(action::get);
    } catch (RuntimeException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new IllegalStateException("Unexpected exception in session scope.", exception);
    }
  }

  @FunctionalInterface
  public interface RequestAction {
    void run() throws IOException, ServletException;
  }
}
