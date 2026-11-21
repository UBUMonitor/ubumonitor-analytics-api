package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out.DatabaseSnapshotPort;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.ConflictException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseConnectionException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.DatabaseUtil;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.JooqProvider;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseSnapshotAdapter implements DatabaseSnapshotPort {

  private static final String DB_EXTENSION = ".db";
  private static final String SNAPSHOT_DIRECTORY = "snapshots";

  private final MoodleConfig moodleConfig;
  private final JooqProvider jooqProvider;

  @Override
  public DatabaseSnapshot createSnapshot(SessionData session) {
    return createSnapshot(session, Instant.now().truncatedTo(ChronoUnit.MILLIS));
  }

  DatabaseSnapshot createSnapshot(SessionData session, Instant createdAt) {
    if (session == null
        || session.getHost() == null
        || session.getUsername() == null
        || session.getDbPassword() == null
        || session.getDbPassword().isBlank()) {
      throw new IllegalArgumentException("A database session is required");
    }

    String basePath = moodleConfig.getDb().getBasePath();
    String databaseId = DatabaseUtil.buildDbFileName(session.getHost(), session.getUsername());
    Path databaseFile = DatabaseUtil.getFile(basePath, session.getHost(), session.getUsername());
    if (!Files.isRegularFile(databaseFile)) {
      throw new EntityNotFoundException("Database", databaseId);
    }

    Path snapshotsDirectory = Path.of(basePath, SNAPSHOT_DIRECTORY);
    String snapshotFileName = DatabaseSnapshotFileNaming.fileName(databaseId, createdAt);
    String snapshotId =
        snapshotFileName.substring(0, snapshotFileName.length() - DB_EXTENSION.length());
    Path destination = snapshotsDirectory.resolve(snapshotFileName);
    Path temporaryFile = null;
    try {
      Files.createDirectories(snapshotsDirectory);
      DataSource dataSource = jooqProvider.getDataSource(session);
      try (Connection connection = dataSource.getConnection()) {
        checkpointWal(connection);
        if (Files.exists(destination)) {
          throw new ConflictException("A snapshot with the same timestamp already exists");
        }
        temporaryFile = Files.createTempFile(snapshotsDirectory, ".snapshot-", ".tmp");
        Files.copy(
            databaseFile,
            temporaryFile,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.COPY_ATTRIBUTES);
        try {
          Files.move(temporaryFile, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (FileAlreadyExistsException exception) {
          throw new ConflictException("A snapshot with the same timestamp already exists");
        } catch (AtomicMoveNotSupportedException exception) {
          throw new IOException("Atomic snapshot move is not supported", exception);
        }
        temporaryFile = null;
      }

      return DatabaseSnapshot.builder()
          .id(snapshotId)
          .databaseId(databaseId)
          .createdAt(createdAt)
          .snapshotFile(destination.toAbsolutePath().normalize().toUri())
          .build();
    } catch (IOException | SQLException exception) {
      throw new DatabaseConnectionException("snapshot", exception);
    } finally {
      if (temporaryFile != null) {
        try {
          Files.deleteIfExists(temporaryFile);
        } catch (IOException exception) {
          log.warn("Failed to remove temporary database snapshot file");
        }
      }
    }
  }

  private void checkpointWal(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement();
        ResultSet checkpoint = statement.executeQuery("PRAGMA wal_checkpoint(TRUNCATE)")) {
      if (!checkpoint.next() || checkpoint.getInt(1) != 0) {
        throw new SQLException("Database WAL checkpoint was busy");
      }
    }
  }
}
