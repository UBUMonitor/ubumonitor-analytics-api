package es.ubu.lsi.ubumonitoranalytics.util;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseInfoUtilTest {

  @TempDir Path tempDir;

  @Test
  void includesTheUriSchemeInTheDatabaseFileName() {
    assertEquals(
        "https~school.moodledemo.net~student",
        DatabaseUtil.buildDbFileName(URI.create("https://school.moodledemo.net"), "student"));
    assertEquals(
        "http~school.moodledemo.net~student",
        DatabaseUtil.buildDbFileName(URI.create("http://school.moodledemo.net"), "student"));
  }

  @Test
  void migratesAnExistingDatabaseToTheSchemePrefixedName() throws Exception {
    URI host = URI.create("https://school.moodledemo.net");
    String oldFileName = "school.moodledemo.net_student";
    String password = "test-password";
    String jdbcTemplate = "jdbc:h2:file:%s;CIPHER=AES;DB_CLOSE_ON_EXIT=FALSE";
    Path oldDatabaseFile = tempDir.resolve(oldFileName + ".mv.db");
    String oldJdbcUrl =
        String.format(
            jdbcTemplate,
            tempDir.resolve(oldFileName).toAbsolutePath().toString().replace("\\", "/"));

    try (var connection = DriverManager.getConnection(oldJdbcUrl, "sa", password + " " + password);
        var statement = connection.createStatement()) {
      statement.execute("CREATE TABLE migration_check (amount INT)");
      statement.execute("INSERT INTO migration_check VALUES (42)");
      statement.execute("SHUTDOWN");
    }

    DatabaseUtil.createAndInitializeDatabase(
        jdbcTemplate, tempDir.toString(), host, "student", password);

    Path newDatabaseFile = DatabaseUtil.getFile(tempDir.toString(), host, "student");
    assertTrue(Files.exists(newDatabaseFile));
    assertFalse(Files.exists(oldDatabaseFile));

    String newJdbcUrl =
        DatabaseUtil.buildJdbcUrl(jdbcTemplate, tempDir.toString(), host, "student");
    try (var connection =
        DriverManager.getConnection(newJdbcUrl, "sa", password + " " + password)) {
      try (var resultSet =
          connection.createStatement().executeQuery("SELECT amount FROM migration_check")) {
        assertTrue(resultSet.next());
        assertEquals(42, resultSet.getInt("amount"));
      }
    }
  }

  @Test
  void migratesAnExistingSchemePrefixedUnderscoreDatabase() throws Exception {
    URI host = URI.create("https://school.moodledemo.net");
    String oldFileName = "https_school.moodledemo.net_student";
    String password = "test-password";
    String jdbcTemplate = "jdbc:h2:file:%s;CIPHER=AES;DB_CLOSE_ON_EXIT=FALSE";
    Path oldDatabaseFile = tempDir.resolve(oldFileName + ".mv.db");
    String oldJdbcUrl =
        String.format(
            jdbcTemplate,
            tempDir.resolve(oldFileName).toAbsolutePath().toString().replace("\\", "/"));

    try (var connection = DriverManager.getConnection(oldJdbcUrl, "sa", password + " " + password);
        var statement = connection.createStatement()) {
      statement.execute("CREATE TABLE migration_check (amount INT)");
      statement.execute("INSERT INTO migration_check VALUES (42)");
      statement.execute("SHUTDOWN");
    }

    DatabaseUtil.createAndInitializeDatabase(
        jdbcTemplate, tempDir.toString(), host, "student", password);

    Path newDatabaseFile = DatabaseUtil.getFile(tempDir.toString(), host, "student");
    assertTrue(Files.exists(newDatabaseFile));
    assertFalse(Files.exists(oldDatabaseFile));

    String newJdbcUrl =
        DatabaseUtil.buildJdbcUrl(jdbcTemplate, tempDir.toString(), host, "student");
    try (var connection =
        DriverManager.getConnection(newJdbcUrl, "sa", password + " " + password)) {
      try (var resultSet =
          connection.createStatement().executeQuery("SELECT amount FROM migration_check")) {
        assertTrue(resultSet.next());
        assertEquals(42, resultSet.getInt("amount"));
      }
    }
  }
}
