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

    assertThrows(UnauthorizedException.class, service::createSnapshot);

    verify(databaseSnapshotPort, never()).createSnapshot(Mockito.any());
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
            .id("https~school.moodledemo.net~teacher~20261121T131300000Z")
            .databaseId("https~school.moodledemo.net~teacher")
            .createdAt(Instant.parse("2026-11-21T13:13:00Z"))
            .snapshotFile(URI.create("file:///data/snapshots/snapshot.db"))
            .build();
    CurrentSessionPort currentSessionPort = Mockito.mock(CurrentSessionPort.class);
    DatabaseSnapshotPort databaseSnapshotPort = Mockito.mock(DatabaseSnapshotPort.class);
    when(currentSessionPort.getSessionData()).thenReturn(session);
    when(databaseSnapshotPort.createSnapshot(session)).thenReturn(expected);
    CreateDatabaseSnapshotService service =
        new CreateDatabaseSnapshotService(currentSessionPort, databaseSnapshotPort);

    assertEquals(expected, service.createSnapshot());

    verify(databaseSnapshotPort).createSnapshot(session);
  }
}
