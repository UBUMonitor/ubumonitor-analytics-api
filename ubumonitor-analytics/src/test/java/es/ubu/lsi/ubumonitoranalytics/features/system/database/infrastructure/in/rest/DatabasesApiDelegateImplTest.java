package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabaseSnapshotInfoDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabaseSnapshotRequestDto;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in.CreateDatabaseSnapshotUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in.DatabaseInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseHostInfo;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseInfo;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class DatabasesApiDelegateImplTest {

  @Test
  void returnsTheCreatedSnapshotFieldsWithStatus201() {
    Instant createdAt = Instant.parse("2026-11-21T13:13:00.123Z");
    DatabaseSnapshot snapshot =
        DatabaseSnapshot.builder()
            .id("https~school.moodledemo.net~teacher~20261121T131300123Z~before sync")
            .databaseId("https~school.moodledemo.net~teacher")
            .name("before sync")
            .createdAt(createdAt)
            .snapshotFile(URI.create("file:///data/snapshots/snapshot.db"))
            .build();
    DatabaseInfoUseCase databaseInfoUseCase = mock(DatabaseInfoUseCase.class);
    CreateDatabaseSnapshotUseCase createSnapshotUseCase = name -> snapshot;
    DatabasesApiDelegateImpl delegate =
        new DatabasesApiDelegateImpl(
            databaseInfoUseCase, createSnapshotUseCase, new DatabasesApiDelegateImplMapperImpl());

    var response = delegate.createDatabaseSnapshot(null);
    DatabaseSnapshotInfoDto body = response.getBody();

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(body);
    assertEquals(snapshot.getId(), body.getId());
    assertEquals(snapshot.getDatabaseId(), body.getDatabaseId());
    assertEquals(snapshot.getName(), body.getName());
    assertEquals(createdAt.atOffset(ZoneOffset.UTC), body.getCreatedAt());
    assertEquals(snapshot.getSnapshotFile(), body.getLocation());
  }

  @Test
  void passesTheRequestNameToTheCreateUseCase() {
    AtomicReference<String> requestedName = new AtomicReference<>();
    DatabaseSnapshot snapshot =
        DatabaseSnapshot.builder()
            .id("https~school.moodledemo.net~teacher~20261121T131300123Z~Before upgrade")
            .databaseId("https~school.moodledemo.net~teacher")
            .name("Before upgrade")
            .createdAt(Instant.parse("2026-11-21T13:13:00.123Z"))
            .snapshotFile(URI.create("file:///data/snapshots/snapshot.db"))
            .build();
    DatabasesApiDelegateImpl delegate =
        new DatabasesApiDelegateImpl(
            mock(DatabaseInfoUseCase.class),
            name -> {
              requestedName.set(name);
              return snapshot;
            },
            new DatabasesApiDelegateImplMapperImpl());

    delegate.createDatabaseSnapshot(new DatabaseSnapshotRequestDto().name("Before upgrade"));

    assertEquals("Before upgrade", requestedName.get());
  }

  @Test
  void includesAnEmptySnapshotsArrayForDatabasesWithoutSnapshots() {
    DatabaseInfo database =
        DatabaseInfo.builder()
            .id("https~school.moodledemo.net~student")
            .userName("student")
            .host(URI.create("https://school.moodledemo.net"))
            .databaseFile(URI.create("file:///data/student.db"))
            .build();
    DatabaseHostInfo host =
        DatabaseHostInfo.builder()
            .host(URI.create("https://school.moodledemo.net"))
            .databases(List.of(database))
            .build();
    DatabaseInfoUseCase databaseInfoUseCase = mock(DatabaseInfoUseCase.class);
    when(databaseInfoUseCase.getDatabasesInfo()).thenReturn(List.of(host));
    DatabasesApiDelegateImpl delegate =
        new DatabasesApiDelegateImpl(
            databaseInfoUseCase,
            mock(CreateDatabaseSnapshotUseCase.class),
            new DatabasesApiDelegateImplMapperImpl());

    var response = delegate.listDatabases();

    assertEquals(
        List.of(),
        response.getBody().getDatabases().getFirst().getUsers().getFirst().getSnapshots());
  }
}
