package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import static org.junit.jupiter.api.Assertions.assertThrows;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.net.URI;
import java.nio.file.Path;
import java.sql.DriverManager;
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
            new MoodleConfig.Db(
                tempDir.toString(),
                "jdbc:h2:file:%s;CIPHER=AES;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1"),
            new MoodleConfig.Api());
    JooqProvider provider = new JooqProvider(config, new AsyncOperations(Runnable::run));
    SessionData session =
        SessionData.builder().host(host).username("teacher").dbPassword("correct-password").build();
    String jdbcUrl =
        DatabaseUtil.buildJdbcUrl(
            config.getDb().getJdbcUrlTemplate(), config.getDb().getBasePath(), host, "teacher");

    try {
      provider.initializeTenant(session);
      session.setDbPassword("wrong-password");

      assertThrows(DatabaseCreationException.class, () -> provider.initializeTenant(session));
    } finally {
      provider.closeTenant(session);
      try (var connection =
          DriverManager.getConnection(jdbcUrl, "sa", "correct-password correct-password")) {
        connection.createStatement().execute("SHUTDOWN");
      }
    }
  }
}
