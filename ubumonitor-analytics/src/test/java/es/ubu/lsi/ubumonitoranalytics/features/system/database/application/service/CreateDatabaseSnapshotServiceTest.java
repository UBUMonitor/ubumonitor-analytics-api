package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out.DatabaseSnapshotPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CreateDatabaseSnapshotServiceTest {

  private CurrentSessionPort currentSessionPort;
  private DatabaseSnapshotPort databaseSnapshotPort;
  private CreateDatabaseSnapshotService service;
  private SessionData session;

  @BeforeEach
  void setUp() {
    currentSessionPort = Mockito.mock(CurrentSessionPort.class);
    databaseSnapshotPort = Mockito.mock(DatabaseSnapshotPort.class);
    session = SessionData.builder().build();
    when(currentSessionPort.getSessionData()).thenReturn(session);
    service = new CreateDatabaseSnapshotService(currentSessionPort, databaseSnapshotPort);
  }

  @Test
  void rejectsRequestsWithoutASession() {
    when(currentSessionPort.getSessionData()).thenReturn(null);

    assertThrows(UnauthorizedException.class, () -> service.createSnapshot(null));

    verify(databaseSnapshotPort, never()).createSnapshot(Mockito.any(), Mockito.any());
  }

  @Test
  void acceptsValidNamesAndPassesThemUnchangedAfterNormalization() {
    List<String> validNames = List.of("abc", "antes de sincronizar", "v1.2_final-3", "a", "A1");

    for (String name : validNames) {
      service.createSnapshot(name);
      verify(databaseSnapshotPort).createSnapshot(session, name);
    }
  }

  @Test
  void rejectsInvalidNamesWithoutCallingThePort() {
    List<String> invalidNames =
        List.of(
            "ñandú",
            "café",
            "snapshot 😀",
            "a/b",
            "a~b",
            "a%b",
            "a+b",
            ".",
            "..",
            "-x",
            "x-",
            "x.",
            "a\nb",
            "a".repeat(65));

    for (String name : invalidNames) {
      assertThrows(BadRequestException.class, () -> service.createSnapshot(name), name);
    }

    verify(databaseSnapshotPort, never()).createSnapshot(Mockito.any(), Mockito.any());
  }

  @Test
  void treatsBlankNamesAsAbsentAndTrimsValidNamesBeforeValidating() {
    service.createSnapshot("  ");
    service.createSnapshot(" abc ");

    verify(databaseSnapshotPort).createSnapshot(session, null);
    verify(databaseSnapshotPort).createSnapshot(session, "abc");
  }
}
