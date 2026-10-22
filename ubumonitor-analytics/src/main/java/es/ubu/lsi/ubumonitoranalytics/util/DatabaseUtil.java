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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

  public static void createAndInitializeDatabase(
      String jdbcTemplate, String basePath, URI host, String username, String dbPassword) {

    Path dir = Path.of(basePath);

    try {
      Files.createDirectories(dir);

      migrateLegacyDatabase(jdbcTemplate, basePath, host, username, dbPassword);
      String jdbcUrl = DatabaseUtil.buildJdbcUrl(jdbcTemplate, basePath, host, username);

      log.info("ABS PATH DB: {}", dir.toAbsolutePath());
      log.info("FINAL FILE: {}", jdbcUrl);
      log.info("FILE EXISTS: {}", Files.exists(getFile(basePath, host, username)));
      log.info("Ensuring database exists for {} / {}", host, username);

      // Opening the database creates it when it does not already exist.
      try (var _ = DriverManager.getConnection(jdbcUrl, "sa", dbPassword + " " + dbPassword)) {
        log.info("Database ready (H2 auto-create)");
      }

    } catch (SQLException e) {
      if (e.getSQLState() != null && e.getSQLState().startsWith("28")) {
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
    return Path.of(basePath).resolve(fileName + ".mv.db");
  }

  private static void migrateLegacyDatabase(
      String jdbcTemplate, String basePath, URI host, String username, String dbPassword)
      throws IOException, SQLException {
    Path databaseFile = getFile(basePath, host, username);
    if (Files.exists(databaseFile)) {
      return;
    }

    for (String legacyFileName : buildLegacyDbFileNames(host, username)) {
      Path legacyDatabaseFile = Path.of(basePath).resolve(legacyFileName + ".mv.db");
      if (!Files.exists(legacyDatabaseFile)) {
        continue;
      }

      String legacyJdbcUrl = buildJdbcUrl(jdbcTemplate, basePath, legacyFileName);
      try (var connection =
              DriverManager.getConnection(legacyJdbcUrl, "sa", dbPassword + " " + dbPassword);
          var statement = connection.createStatement()) {
        statement.execute("SHUTDOWN");
      }

      Files.move(legacyDatabaseFile, databaseFile);
      return;
    }
  }

  private static String[] buildLegacyDbFileNames(URI host, String username) {
    String hostName = toHostString(host);
    String safeUserName = toSafeUserName(username);
    String scheme = host == null ? null : host.getScheme();
    if (scheme == null || scheme.isBlank()) {
      return new String[] {hostName + "_" + safeUserName};
    }
    return new String[] {
      scheme.toLowerCase(Locale.ROOT) + "_" + hostName + "_" + safeUserName,
      hostName + "_" + safeUserName
    };
  }

  private static String buildJdbcUrl(String jdbcTemplate, String basePath, String fileName) {
    String fullPath = Path.of(basePath).resolve(fileName).toAbsolutePath().toString();
    // H2 JDBC URLs are more portable when Windows path separators are normalized.
    return String.format(jdbcTemplate, fullPath.replace("\\", "/"));
  }
}
