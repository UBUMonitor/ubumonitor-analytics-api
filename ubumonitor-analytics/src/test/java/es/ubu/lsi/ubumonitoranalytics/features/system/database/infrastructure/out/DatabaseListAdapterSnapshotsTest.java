package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseListAdapterSnapshotsTest {

  @TempDir Path tempDirectory;

  @Test
  void associatesSnapshotsInDescendingOrderAndIgnoresInvalidAndOrphanedFiles() throws Exception {
    Files.createFile(tempDirectory.resolve("https~school.moodledemo.net~student.db"));
    Files.createFile(tempDirectory.resolve("https~school.moodledemo.net~teacher.db"));
    Path snapshotsDirectory = Files.createDirectory(tempDirectory.resolve("snapshots"));
    Files.createFile(
        snapshotsDirectory.resolve("https~school.moodledemo.net~student~20261121T131300000Z.db"));
    Files.createFile(
        snapshotsDirectory.resolve("https~school.moodledemo.net~student~20261121T131400000Z.db"));
    Files.createFile(
        snapshotsDirectory.resolve(
            "https~school.moodledemo.net~student~20261121T131450000Z~Before upgrade.db"));
    Files.createFile(
        snapshotsDirectory.resolve("https~school.moodledemo.net~teacher~20261121T131200000Z.db"));
    Files.createFile(snapshotsDirectory.resolve("orphan~20261121T131500000Z.db"));
    Files.createFile(snapshotsDirectory.resolve("invalid-snapshot.db"));
    Files.createFile(
        snapshotsDirectory.resolve("https~school.moodledemo.net~student~20260230T131300000Z.db"));

    List<DatabaseInfo> databases =
        new DatabaseListAdapter(config()).getDatabaseList().getFirst().getDatabases();

    DatabaseInfo student =
        databases.stream()
            .filter(database -> database.getUserName().equals("student"))
            .findFirst()
            .orElseThrow();
    DatabaseInfo teacher =
        databases.stream()
            .filter(database -> database.getUserName().equals("teacher"))
            .findFirst()
            .orElseThrow();
    assertEquals(
        List.of(
            "https~school.moodledemo.net~student~20261121T131450000Z~Before upgrade",
            "https~school.moodledemo.net~student~20261121T131400000Z",
            "https~school.moodledemo.net~student~20261121T131300000Z"),
        student.getSnapshots().stream().map(snapshot -> snapshot.getId()).toList());
    assertEquals("Before upgrade", student.getSnapshots().getFirst().getName());
    assertEquals(
        snapshotsDirectory
            .resolve("https~school.moodledemo.net~student~20261121T131450000Z~Before upgrade.db")
            .toUri(),
        student.getSnapshots().getFirst().getSnapshotFile());
    assertEquals(
        List.of("https~school.moodledemo.net~teacher~20261121T131200000Z"),
        teacher.getSnapshots().stream().map(snapshot -> snapshot.getId()).toList());
  }

  @Test
  void returnsAnEmptySnapshotListWhenTheSnapshotsDirectoryDoesNotExist() throws Exception {
    Files.createFile(tempDirectory.resolve("https~school.moodledemo.net~student.db"));

    List<DatabaseInfo> databases =
        new DatabaseListAdapter(config()).getDatabaseList().getFirst().getDatabases();

    assertEquals(1, databases.size());
    assertEquals(List.of(), databases.getFirst().getSnapshots());
  }

  private MoodleConfig config() {
    return new MoodleConfig(
        new MoodleConfig.Db(tempDirectory.toString()), new MoodleConfig.Api(null, null));
  }
}
