package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out.DatabaseSnapshotPort;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CreateDatabaseSnapshotServiceTest {

  @Test
  void rejectsRequestsWithoutASession() {
    CurrentSessionPort currentSessionPort = Mockito.mock(CurrentSessionPort.class);
    DatabaseSnapshotPort databaseSnapshotPort = Mockito.mock(DatabaseSnapshotPort.class);
    when(currentSessionPort.getSessionData()).thenReturn(null);
    CreateDatabaseSnapshotService service =
        new CreateDatabaseSnapshotService(currentSessionPort, databaseSnapshotPort);

    assertThrows(UnauthorizedException.class, () -> service.createSnapshot(null));

    verify(databaseSnapshotPort, never()).createSnapshot(Mockito.any(), Mockito.any());
  }

  @Test
  void delegatesSnapshotCreationWithTheCurrentSession() {
    SessionData session =
        SessionData.builder()
            .host(URI.create("https://school.moodledemo.net"))
            .username("teacher")
            .dbPassword("test-password")
            .build();
    DatabaseSnapshot expected =
        DatabaseSnapshot.builder()
            .id("https~school.moodledemo.net~teacher~20261121T131300000Z~abc")
            .databaseId("https~school.moodledemo.net~teacher")
            .name("abc")
            .createdAt(Instant.parse("2026-11-21T13:13:00Z"))
            .snapshotFile(URI.create("file:///data/snapshots/snapshot.db"))
            .build();
    CurrentSessionPort currentSessionPort = Mockito.mock(CurrentSessionPort.class);
    DatabaseSnapshotPort databaseSnapshotPort = Mockito.mock(DatabaseSnapshotPort.class);
    when(currentSessionPort.getSessionData()).thenReturn(session);
    when(databaseSnapshotPort.createSnapshot(session, "abc")).thenReturn(expected);
    CreateDatabaseSnapshotService service =
        new CreateDatabaseSnapshotService(currentSessionPort, databaseSnapshotPort);

    assertEquals(expected, service.createSnapshot(" abc "));

    verify(databaseSnapshotPort).createSnapshot(session, "abc");
  }

  @Test
  void normalizesMissingAndBlankNamesToNull() {
    SessionData session = SessionData.builder().build();
    CurrentSessionPort currentSessionPort = Mockito.mock(CurrentSessionPort.class);
    DatabaseSnapshotPort databaseSnapshotPort = Mockito.mock(DatabaseSnapshotPort.class);
    when(currentSessionPort.getSessionData()).thenReturn(session);
    CreateDatabaseSnapshotService service =
        new CreateDatabaseSnapshotService(currentSessionPort, databaseSnapshotPort);

    service.createSnapshot(null);
    service.createSnapshot("  ");

    verify(databaseSnapshotPort, Mockito.times(2)).createSnapshot(session, null);
  }
}
