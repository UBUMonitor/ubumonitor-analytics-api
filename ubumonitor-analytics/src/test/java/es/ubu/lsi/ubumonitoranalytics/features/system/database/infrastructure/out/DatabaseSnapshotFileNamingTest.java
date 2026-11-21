package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseSnapshotFileNamingTest {

  @TempDir Path temporaryDirectory;

  @Test
  void encodesUnicodeNamesAndDecodesThemFromListings() throws Exception {
    Instant createdAt = Instant.parse("2026-11-21T13:13:00.123Z");
    String fileName =
        DatabaseSnapshotFileNaming.fileName(
            "https~school.moodledemo.net~teacher", createdAt, "Café 東京");

    assertEquals(
        "https~school.moodledemo.net~teacher~20261121T131300123Z~Caf%C3%A9+%E6%9D%B1%E4%BA%AC.db",
        fileName);

    Path snapshotFile = temporaryDirectory.resolve(fileName);
    Files.createFile(snapshotFile);
    DatabaseSnapshot snapshot = DatabaseSnapshotFileNaming.fromPath(snapshotFile).orElseThrow();
    assertEquals("Café 東京", snapshot.getName());
    assertEquals(fileName.substring(0, fileName.length() - ".db".length()), snapshot.getId());
  }

  @Test
  void roundTripsANameContainingTheSnapshotSeparator() throws Exception {
    Instant createdAt = Instant.parse("2026-11-21T13:13:00.123Z");
    String fileName =
        DatabaseSnapshotFileNaming.fileName(
            "https~school.moodledemo.net~teacher", createdAt, "before~sync");

    assertEquals(
        "https~school.moodledemo.net~teacher~20261121T131300123Z~before%7Esync.db", fileName);
    Path snapshotFile = temporaryDirectory.resolve(fileName);
    Files.createFile(snapshotFile);

    DatabaseSnapshot snapshot = DatabaseSnapshotFileNaming.fromPath(snapshotFile).orElseThrow();

    assertEquals("before~sync", snapshot.getName());
    assertEquals(fileName.substring(0, fileName.length() - ".db".length()), snapshot.getId());
  }

  @Test
  void leavesTheLegacyFilenameUnchangedWhenNameIsAbsent() {
    assertEquals(
        "https~school.moodledemo.net~teacher~20261121T131300123Z.db",
        DatabaseSnapshotFileNaming.fileName(
            "https~school.moodledemo.net~teacher",
            Instant.parse("2026-11-21T13:13:00.123Z"),
            null));
  }

  @Test
  void rejectsNamesWhoseEncodedFormExceedsOneHundredCharacters() {
    String name = "ä".repeat(60);
    assertThrows(
        BadRequestException.class,
        () ->
            DatabaseSnapshotFileNaming.fileName(
                "https~school.moodledemo.net~teacher",
                Instant.parse("2026-11-21T13:13:00Z"),
                name));
  }

  @Test
  void ignoresMalformedPercentEncodingWhenListingSnapshots() throws Exception {
    Path snapshotFile =
        temporaryDirectory.resolve(
            "https~school.moodledemo.net~teacher~20261121T131300123Z~bad%ZZ.db");
    Files.createFile(snapshotFile);

    assertEquals(java.util.Optional.empty(), DatabaseSnapshotFileNaming.fromPath(snapshotFile));
  }
}
