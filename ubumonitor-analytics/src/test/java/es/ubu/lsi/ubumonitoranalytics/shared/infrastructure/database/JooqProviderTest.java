package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.InvalidDatabaseEncryptionKeyException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.net.URI;
import java.nio.file.Path;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JooqProviderTest {

  @TempDir Path tempDir;

  @Test
  void rejectsAnIncorrectPasswordForAnExistingTenantDatabase() throws SQLException {
    URI host = URI.create("https://moodle.test");
    MoodleConfig config =
        new MoodleConfig(
            new MoodleConfig.Db(tempDir.toString(), "jdbc:sqlite:%s"), new MoodleConfig.Api());
    JooqProvider provider = new JooqProvider(config);
    SessionData session =
        SessionData.builder().host(host).username("teacher").dbPassword("correct-password").build();
    try {
      assertTrue(provider.initializeTenant(session));
      assertFalse(provider.initializeTenant(session));
      session.setDbPassword("wrong-password");

      assertThrows(
          InvalidDatabaseEncryptionKeyException.class, () -> provider.initializeTenant(session));
    } finally {
      provider.closeTenant(session);
    }
  }

  @Test
  void migratesTheTenantDatabaseAndCreatesTheSqliteFile() throws SQLException {
    URI host = URI.create("https://moodle.test");
    String username = "teacher";
    String password = "correct-password";
    String jdbcTemplate = "jdbc:sqlite:%s";
    MoodleConfig config =
        new MoodleConfig(
            new MoodleConfig.Db(tempDir.toString(), jdbcTemplate), new MoodleConfig.Api());
    JooqProvider provider = new JooqProvider(config);
    SessionData session =
        SessionData.builder().host(host).username(username).dbPassword(password).build();

    try {
      assertTrue(provider.initializeTenant(session));
      assertEquals(
          1,
          provider
              .getDSLContext(session)
              .fetchCount(
                  provider
                      .getDSLContext(session)
                      .selectOne()
                      .from("sqlite_master")
                      .where("type = 'table' AND name = 'USERS'")));
      assertTrue(DatabaseUtil.exists(tempDir.toString(), host, username));
    } finally {
      provider.closeTenant(session);
    }
  }
}
