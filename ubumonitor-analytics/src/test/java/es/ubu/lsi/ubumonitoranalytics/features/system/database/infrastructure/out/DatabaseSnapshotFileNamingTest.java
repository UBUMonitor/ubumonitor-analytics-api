package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseSnapshotFileNamingTest {

  @TempDir Path temporaryDirectory;

  @Test
  void roundTripsNameAndSpacesLiterally() throws Exception {
    Instant createdAt = Instant.parse("2026-11-21T13:13:00.123Z");
    String fileName =
        DatabaseSnapshotFileNaming.fileName(
            "https~school.moodledemo.net~teacher", createdAt, "Before upgrade");

    assertEquals(
        "https~school.moodledemo.net~teacher~20261121T131300123Z~Before upgrade.db", fileName);

    Path snapshotFile = temporaryDirectory.resolve(fileName);
    Files.createFile(snapshotFile);
    DatabaseSnapshot snapshot = DatabaseSnapshotFileNaming.fromPath(snapshotFile).orElseThrow();

    assertEquals("Before upgrade", snapshot.getName());
    assertEquals("https~school.moodledemo.net~teacher", snapshot.getDatabaseId());
    assertEquals(fileName.substring(0, fileName.length() - ".db".length()), snapshot.getId());
  }

  @Test
  void keepsLegacyFilenameWhenNameIsAbsent() throws Exception {
    String fileName =
        DatabaseSnapshotFileNaming.fileName(
            "https~school.moodledemo.net~teacher", Instant.parse("2026-11-21T13:13:00.123Z"), null);
    Path snapshotFile = temporaryDirectory.resolve(fileName);
    Files.createFile(snapshotFile);

    DatabaseSnapshot snapshot = DatabaseSnapshotFileNaming.fromPath(snapshotFile).orElseThrow();

    assertEquals("https~school.moodledemo.net~teacher~20261121T131300123Z.db", fileName);
    assertNull(snapshot.getName());
  }

  @Test
  void ignoresFilenameWithoutValidTimestamp() throws Exception {
    Path snapshotFile =
        temporaryDirectory.resolve("https~school.moodledemo.net~teacher~not-a-timestamp.db");
    Files.createFile(snapshotFile);

    assertEquals(java.util.Optional.empty(), DatabaseSnapshotFileNaming.fromPath(snapshotFile));
  }
}
