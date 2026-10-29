package es.ubu.lsi.ubumonitoranalytics.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.InvalidDatabaseEncryptionKeyException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseInfoUtilTest {

  @TempDir Path tempDir;

  @Test
  void createsAnEncryptedChacha20DatabaseAndRejectsAnIncorrectKey() throws Exception {
    URI host = URI.create("https://school.moodledemo.net");
    String username = "student";
    String password = "test-password";
    String jdbcTemplate = "jdbc:sqlite:%s";

    assertTrue(
        DatabaseUtil.createAndInitializeDatabase(
            jdbcTemplate, tempDir.toString(), host, username, password));

    Path databaseFile = DatabaseUtil.getFile(tempDir.toString(), host, username);
    assertTrue(Files.exists(databaseFile));
    try (var connection =
            DatabaseUtil.createDataSource(
                    DatabaseUtil.buildJdbcUrl(jdbcTemplate, tempDir.toString(), host, username),
                    password)
                .getConnection();
        var statement = connection.createStatement()) {
      statement.execute("CREATE TABLE cipher_probe (id INTEGER PRIMARY KEY)");
    }
    assertFalse(
        new String(Files.readAllBytes(databaseFile), StandardCharsets.ISO_8859_1)
            .startsWith("SQLite format 3"));
    assertFalse(
        DatabaseUtil.createAndInitializeDatabase(
            jdbcTemplate, tempDir.toString(), host, username, password));

    assertThrows(
        InvalidDatabaseEncryptionKeyException.class,
        () ->
            DatabaseUtil.createAndInitializeDatabase(
                jdbcTemplate, tempDir.toString(), host, username, "wrong-password"));
  }

  @Test
  void includesTheUriSchemeInTheDatabaseFileName() {
    assertEquals(
        "https~school.moodledemo.net~student",
        DatabaseUtil.buildDbFileName(URI.create("https://school.moodledemo.net"), "student"));
    assertEquals(
        "http~school.moodledemo.net~student",
        DatabaseUtil.buildDbFileName(URI.create("http://school.moodledemo.net"), "student"));
  }
}
