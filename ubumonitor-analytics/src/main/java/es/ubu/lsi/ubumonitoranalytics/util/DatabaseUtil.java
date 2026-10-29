package es.ubu.lsi.ubumonitoranalytics.util;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.InvalidDatabaseEncryptionKeyException;
import java.net.IDN;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.sqlite.mc.SQLiteMCChacha20Config;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DatabaseUtil {

  public static String toHostString(URI host) {
    if (host == null || host.getHost() == null) {
      return "unknown_host";
    }
    return IDN.toASCII(host.getHost());
  }

  public static String toSafeUserName(String username) {
    if (username == null || username.isBlank()) {
      return "default_user";
    }
    // URL encoding preserves username identity while producing a file-system-safe value.
    return URLEncoder.encode(username, StandardCharsets.UTF_8);
  }

  public static String buildDbFileName(URI host, String username) {
    String scheme = host == null ? null : host.getScheme();
    String protocolPrefix =
        scheme == null || scheme.isBlank() ? "" : scheme.toLowerCase(Locale.ROOT) + "~";
    return protocolPrefix + toHostString(host) + "~" + toSafeUserName(username);
  }

  public static String buildJdbcUrl(
      String jdbcTemplate, String basePath, URI host, String username) {
    return buildJdbcUrl(jdbcTemplate, basePath, buildDbFileName(host, username));
  }

  public static boolean createAndInitializeDatabase(
      String jdbcTemplate, String basePath, URI host, String username, String dbPassword) {

    Path dir = Path.of(basePath);

    try {
      Files.createDirectories(dir);

      boolean databaseCreated = !Files.exists(getFile(basePath, host, username));
      String jdbcUrl = DatabaseUtil.buildJdbcUrl(jdbcTemplate, basePath, host, username);

      log.info("Ensuring database exists for {} / {}", host, username);

      try (Connection connection = createDataSource(jdbcUrl, dbPassword).getConnection();
          var statement = connection.createStatement();
          var result = statement.executeQuery("SELECT count(*) FROM sqlite_master")) {
        result.next();
        log.info("Encrypted SQLite database is ready");
      }
      return databaseCreated;

    } catch (SQLException e) {
      if (isEncryptionKeyFailure(e)) {
        throw new InvalidDatabaseEncryptionKeyException("Invalid database encryption key", e);
      }
      throw new DatabaseCreationException(e.getMessage(), e);
    } catch (Exception e) {
      throw new DatabaseCreationException("Unexpected error", e);
    }
  }

  public static boolean exists(String basePath, URI host, String username) {
    return Files.exists(getFile(basePath, host, username));
  }

  public static Path getFile(String basePath, URI host, String username) {
    String fileName = DatabaseUtil.buildDbFileName(host, username);
    return Path.of(basePath).resolve(fileName + ".db");
  }

  public static SQLiteDataSource createDataSource(String jdbcUrl, String dbPassword) {
    SQLiteConfig config = SQLiteMCChacha20Config.getDefault().withKey(dbPassword).build();
    config.setBusyTimeout(30_000);
    config.enforceForeignKeys(true);

    SQLiteDataSource dataSource = new SQLiteDataSource(config);
    dataSource.setUrl(jdbcUrl);
    return dataSource;
  }

  private static boolean isEncryptionKeyFailure(SQLException exception) {
    for (SQLException current = exception; current != null; current = current.getNextException()) {
      String message = current.getMessage();
      if (message != null
          && (message.toLowerCase(Locale.ROOT).contains("not a database")
              || message.toLowerCase(Locale.ROOT).contains("encrypted"))) {
        return true;
      }
    }
    return false;
  }

  private static String buildJdbcUrl(String jdbcTemplate, String basePath, String fileName) {
    String fullPath = Path.of(basePath).resolve(fileName + ".db").toAbsolutePath().toString();
    // SQLite JDBC URLs are more portable when Windows path separators are normalized.
    return String.format(jdbcTemplate, fullPath.replace("\\", "/"));
  }
}
