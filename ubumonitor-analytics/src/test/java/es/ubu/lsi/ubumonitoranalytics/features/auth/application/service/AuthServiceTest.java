package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database.TenantDatabasePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class AuthServiceTest {

  @Test
  void doesNotInitializeTenantDatabaseWhenMoodleRejectsCredentials() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);

    when(moodleApiPort.login(any())).thenReturn(null);
    when(moodleApiPort.loginWeb("teacher", "wrong-password", URI.create("https://moodle.test")))
        .thenReturn(mock(RestClient.class));

    AuthService authService =
        new AuthService(sessionPort, moodleApiPort, tenantDatabasePort, asyncOperations);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "wrong-password",
            URI.create("https://moodle.test"),
            "db-password",
            null,
            null);

    assertThrows(UnauthorizedException.class, () -> authService.loginByCredentials(authInput));
    verify(moodleApiPort).login(authInput);
    verifyNoInteractions(tenantDatabasePort, sessionPort);
  }

  @Test
  void doesNotCreateSessionWhenTenantDatabasePasswordIsRejected() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "wrong-database-password",
            null,
            null);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(mock(RestClient.class));
    doThrow(new DatabaseCreationException("Invalid database password", new IllegalStateException()))
        .when(tenantDatabasePort)
        .initializeTenant(any());

    AuthService authService =
        new AuthService(sessionPort, moodleApiPort, tenantDatabasePort, asyncOperations);

    assertThrows(DatabaseCreationException.class, () -> authService.loginByCredentials(authInput));
    verify(tenantDatabasePort).initializeTenant(any());
    verifyNoInteractions(sessionPort);
  }
}
