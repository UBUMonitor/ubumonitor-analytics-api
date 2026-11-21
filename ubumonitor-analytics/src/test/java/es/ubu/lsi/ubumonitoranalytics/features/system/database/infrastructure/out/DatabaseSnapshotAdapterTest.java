package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.ConflictException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.DatabaseUtil;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.JooqProvider;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class DatabaseSnapshotAdapterTest {

  private static final URI HOST = URI.create("https://school.moodledemo.net");
  private static final String USERNAME = "teacher";
  private static final String ENCRYPTION_KEY = "unit-test-encryption-key";
  private static final Instant CREATED_AT = Instant.parse("2026-11-21T13:13:00.123Z");

  @TempDir Path temporaryDirectory;

  private MoodleConfig moodleConfig;
  private SessionData sessionData;
  private JooqProvider jooqProvider;
  private DatabaseSnapshotAdapter snapshotAdapter;

  @BeforeEach
  void setUp() {
    moodleConfig = new MoodleConfig();
    moodleConfig.getDb().setBasePath(temporaryDirectory.toString());
    sessionData =
        SessionData.builder().host(HOST).username(USERNAME).dbPassword(ENCRYPTION_KEY).build();
    jooqProvider = Mockito.mock(JooqProvider.class);
    snapshotAdapter =
        new DatabaseSnapshotAdapter(
            moodleConfig, jooqProvider, Clock.fixed(CREATED_AT, ZoneOffset.UTC));
  }

  @Test
  void createsAnEncryptedSnapshotUsingTheSameKey() throws Exception {
    createDatabaseWithRow();
    DatabaseSnapshot snapshot = snapshotAdapter.createSnapshot(sessionData, null);

    Path snapshotFile = Path.of(snapshot.getSnapshotFile());
    assertTrue(Files.isRegularFile(snapshotFile));
    assertEquals(temporaryDirectory.resolve("snapshots"), snapshotFile.getParent());
    assertEquals("https~school.moodledemo.net~teacher", snapshot.getDatabaseId());
    assertEquals("https~school.moodledemo.net~teacher~20261121T131300123Z", snapshot.getId());
    assertNull(snapshot.getName());
    assertEquals(CREATED_AT, snapshot.getCreatedAt());

    DataSource snapshotDataSource =
        DatabaseUtil.createDataSource(
            "jdbc:sqlite:" + snapshotFile.toString().replace('\\', '/'), ENCRYPTION_KEY);
    try (Connection connection = snapshotDataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rows = statement.executeQuery("SELECT VALUE FROM SNAPSHOT_TEST")) {
      assertTrue(rows.next());
      assertEquals("preserved", rows.getString(1));
    }

    DataSource invalidKeyDataSource =
        DatabaseUtil.createDataSource(
            "jdbc:sqlite:" + snapshotFile.toString().replace('\\', '/'), "incorrect-key");
    assertThrows(
        SQLException.class,
        () -> {
          try (Connection connection = invalidKeyDataSource.getConnection();
              Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT count(*) FROM sqlite_master");
          }
        });
  }

  @Test
  void createsAnEncryptedSnapshotWithTheRequestedName() throws Exception {
    createDatabaseWithRow();
    DatabaseSnapshot snapshot = snapshotAdapter.createSnapshot(sessionData, "Before upgrade");

    Path snapshotFile = Path.of(snapshot.getSnapshotFile());
    assertTrue(Files.isRegularFile(snapshotFile));
    assertEquals(
        "https~school.moodledemo.net~teacher~20261121T131300123Z~Before upgrade.db",
        snapshotFile.getFileName().toString());
    assertEquals("Before upgrade", snapshot.getName());
    assertEquals(
        "https~school.moodledemo.net~teacher~20261121T131300123Z~Before upgrade", snapshot.getId());

    DataSource snapshotDataSource =
        DatabaseUtil.createDataSource(
            "jdbc:sqlite:" + snapshotFile.toString().replace('\\', '/'), ENCRYPTION_KEY);
    try (Connection connection = snapshotDataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rows = statement.executeQuery("SELECT VALUE FROM SNAPSHOT_TEST")) {
      assertTrue(rows.next());
      assertEquals("preserved", rows.getString(1));
    }
  }

  @Test
  void failsWhenTheSourceDatabaseDoesNotExist() {
    assertThrows(
        EntityNotFoundException.class, () -> snapshotAdapter.createSnapshot(sessionData, null));
  }

  @Test
  void failsWhenASnapshotWithTheSameTimestampAlreadyExists() throws Exception {
    createDatabaseWithRow();
    Path snapshotsDirectory = temporaryDirectory.resolve("snapshots");
    Files.createDirectories(snapshotsDirectory);
    Files.createFile(
        snapshotsDirectory.resolve(
            DatabaseSnapshotFileNaming.fileName(
                DatabaseUtil.buildDbFileName(HOST, USERNAME), CREATED_AT, null)));

    assertThrows(ConflictException.class, () -> snapshotAdapter.createSnapshot(sessionData, null));
  }

  private void createDatabaseWithRow() throws Exception {
    assertTrue(
        DatabaseUtil.createAndInitializeDatabase(
            moodleConfig.getDb().getBasePath(), HOST, USERNAME, ENCRYPTION_KEY));
    DataSource dataSource =
        DatabaseUtil.createDataSource(
            DatabaseUtil.buildJdbcUrl(moodleConfig.getDb().getBasePath(), HOST, USERNAME),
            ENCRYPTION_KEY);
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("CREATE TABLE SNAPSHOT_TEST (ID INTEGER PRIMARY KEY, VALUE TEXT)");
      statement.execute("INSERT INTO SNAPSHOT_TEST (VALUE) VALUES ('preserved')");
    }
    when(jooqProvider.getDataSource(sessionData)).thenReturn(dataSource);
  }
}
