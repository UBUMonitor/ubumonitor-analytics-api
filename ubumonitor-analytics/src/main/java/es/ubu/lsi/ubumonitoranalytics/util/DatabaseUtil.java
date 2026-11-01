package es.ubu.lsi.ubumonitoranalytics.util;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.InvalidDatabaseEncryptionKeyException;
import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.mc.SQLiteMCChacha20Config;

/** Helpers to name, locate, create and open the per-user encrypted SQLite databases. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DatabaseUtil {

  /** Default SQLite page cache size (native memory, outside the Java heap), in KB. */
  public static final int DEFAULT_CACHE_SIZE_KB = 32_768;

  /** Page size applied only when the database file is created. */
  private static final int PAGE_SIZE_BYTES = 8192;

  private static final int BUSY_TIMEOUT_MS = 30_000;

  private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";
  private static final String DB_FILE_EXTENSION = ".db";
  private static final String FILE_NAME_SEPARATOR = "~";
  private static final String UNKNOWN_HOST = "unknown_host";
  private static final String DEFAULT_USER = "default_user";

  /** Message fragments (lower case) SQLite reports when the encryption key is wrong. */
  private static final List<String> ENCRYPTION_KEY_ERROR_MARKERS =
      List.of("not a database", "encrypted");

  // ---------------------------------------------------------------------------
  // Naming and location
  // ---------------------------------------------------------------------------

  public static String toHostString(URI host) {
    if (host == null || host.getHost() == null) {
      return UNKNOWN_HOST;
    }
    return IDN.toASCII(host.getHost());
  }

  public static String toSafeUserName(String username) {
    if (username == null || username.isBlank()) {
      return DEFAULT_USER;
    }
    // URL encoding preserves username identity while producing a file-system-safe value.
    return URLEncoder.encode(username, StandardCharsets.UTF_8);
  }

  public static String buildDbFileName(URI host, String username) {
    return schemePrefix(host) + toHostString(host) + FILE_NAME_SEPARATOR + toSafeUserName(username);
  }

  public static Path getFile(String basePath, URI host, String username) {
    return Path.of(basePath).resolve(buildDbFileName(host, username) + DB_FILE_EXTENSION);
  }

  public static boolean exists(String basePath, URI host, String username) {
    return Files.exists(getFile(basePath, host, username));
  }

  public static String buildJdbcUrl(String basePath, URI host, String username) {
    return toJdbcUrl(getFile(basePath, host, username));
  }

  // ---------------------------------------------------------------------------
  // Creation
  // ---------------------------------------------------------------------------

  public static boolean createAndInitializeDatabase(
      String basePath, URI host, String username, String dbPassword) {
    return createAndInitializeDatabase(basePath, host, username, dbPassword, DEFAULT_CACHE_SIZE_KB);
  }

  /**
   * Creates the database if needed and validates that it can be opened with the given key.
   *
   * @return {@code true} if the database file did not exist and has just been created
   * @throws InvalidDatabaseEncryptionKeyException if the key does not match an existing database
   * @throws DatabaseCreationException for any other failure
   */
  public static boolean createAndInitializeDatabase(
      String basePath, URI host, String username, String dbPassword, int cacheSizeKb) {

    Path dbFile = getFile(basePath, host, username);

    try {
      Files.createDirectories(dbFile.getParent());

      // Must be computed BEFORE opening: page_size and WAL only apply on first creation.
      boolean databaseCreated = Files.notExists(dbFile);

      log.info("Ensuring database exists for {} / {}", host, username);

      SQLiteDataSource dataSource =
          createDataSource(toJdbcUrl(dbFile), dbPassword, databaseCreated, cacheSizeKb);
      validateConnection(dataSource);

      log.info("Encrypted SQLite database is ready");
      return databaseCreated;

    } catch (SQLException e) {
      if (isEncryptionKeyFailure(e)) {
        throw new InvalidDatabaseEncryptionKeyException("Invalid database encryption key", e);
      }
      throw new DatabaseCreationException(e.getMessage(), e);
    } catch (IOException e) {
      throw new DatabaseCreationException("Could not create database directory", e);
    } catch (RuntimeException e) {
      throw new DatabaseCreationException("Unexpected error", e);
    }
  }

  // ---------------------------------------------------------------------------
  // Data source
  // ---------------------------------------------------------------------------

  /** Backwards compatible overload: existing database, default cache size. */
  public static SQLiteDataSource createDataSource(String jdbcUrl, String dbPassword) {
    return createDataSource(jdbcUrl, dbPassword, false, DEFAULT_CACHE_SIZE_KB);
  }

  /**
   * Builds an encrypted (ChaCha20) SQLite data source.
   *
   * <p>The key is applied through {@link SQLiteConfig}, never through the URL, so it cannot leak
   * into logs. Settings that are per-connection (cache, synchronous, busy timeout) are applied on
   * every new connection. Settings stored in the file (page size, WAL) are applied only when the
   * database is being created.
   *
   * @param firstCreation true when the database file does not exist yet
   * @param cacheSizeKb page cache size in KB (native memory, not counted in -Xmx)
   */
  public static SQLiteDataSource createDataSource(
      String jdbcUrl, String dbPassword, boolean firstCreation, int cacheSizeKb) {

    SQLiteConfig config = SQLiteMCChacha20Config.getDefault().withKey(dbPassword).build();

    // Per connection (not persisted in the file). Negative cache size = size in KB.
    config.setBusyTimeout(BUSY_TIMEOUT_MS);
    config.enforceForeignKeys(true);
    config.setCacheSize(-Math.abs(cacheSizeKb));
    config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

    // Persisted in the file: only when creating it.
    if (firstCreation) {
      config.setPageSize(PAGE_SIZE_BYTES);
      config.setJournalMode(SQLiteConfig.JournalMode.WAL);
    }

    SQLiteDataSource dataSource = new SQLiteDataSource(config);
    dataSource.setUrl(jdbcUrl);
    return dataSource;
  }

  // ---------------------------------------------------------------------------
  // Internals
  // ---------------------------------------------------------------------------

  /**
   * Opens a short-lived connection used only to create/validate the database. It is closed before
   * the application pool is opened so two connections never write in parallel.
   */
  private static void validateConnection(SQLiteDataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        var statement = connection.createStatement();
        var result = statement.executeQuery("SELECT count(*) FROM sqlite_master")) {
      result.next();
    }
  }

  private static String schemePrefix(URI host) {
    String scheme = host == null ? null : host.getScheme();
    if (scheme == null || scheme.isBlank()) {
      return "";
    }
    return scheme.toLowerCase(Locale.ROOT) + FILE_NAME_SEPARATOR;
  }

  private static String toJdbcUrl(Path dbFile) {
    // SQLite JDBC URLs are more portable when Windows path separators are normalized.
    return JDBC_URL_PREFIX + dbFile.toAbsolutePath().toString().replace('\\', '/');
  }

  private static boolean isEncryptionKeyFailure(SQLException exception) {
    for (SQLException current = exception; current != null; current = current.getNextException()) {
      String message = current.getMessage();
      if (message == null) {
        continue;
      }
      String lowerCaseMessage = message.toLowerCase(Locale.ROOT);
      if (ENCRYPTION_KEY_ERROR_MARKERS.stream().anyMatch(lowerCaseMessage::contains)) {
        return true;
      }
    }
    return false;
  }
}
