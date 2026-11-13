package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.InvalidDatabaseEncryptionKeyException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import java.net.URI;
import java.nio.file.Path;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

class JooqProviderTest {

  @TempDir Path tempDir;

  @Test
  void rejectsAnIncorrectPasswordForAnExistingTenantDatabase() throws SQLException {
    URI host = URI.create("https://moodle.test");
    MoodleConfig config =
        new MoodleConfig(new MoodleConfig.Db(tempDir.toString()), new MoodleConfig.Api());
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
    MoodleConfig config =
        new MoodleConfig(new MoodleConfig.Db(tempDir.toString()), new MoodleConfig.Api());
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
                      .where("type = 'table' AND name = 'users'")));
      assertTrue(DatabaseUtil.exists(tempDir.toString(), host, username));
    } finally {
      provider.closeTenant(session);
    }
  }

  @Test
  void tenantDatasourceLogsDoNotContainTheTenantPath() throws SQLException {
    URI host = URI.create("https://moodle.test");
    MoodleConfig config =
        new MoodleConfig(new MoodleConfig.Db(tempDir.toString()), new MoodleConfig.Api());
    JooqProvider provider = new JooqProvider(config);
    SessionData session =
        SessionData.builder()
            .host(host)
            .username("secret-username")
            .dbPassword("secret-key")
            .build();
    Logger logger = (Logger) LoggerFactory.getLogger(JooqProvider.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    try {
      provider.initializeTenant(session);

      String logMessages =
          appender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .collect(java.util.stream.Collectors.joining("\n"));
      assertTrue(logMessages.contains("Creating tenant datasource"));
      assertFalse(logMessages.contains(tempDir.toString()));
      assertFalse(logMessages.contains("secret-username"));
    } finally {
      provider.closeTenant(session);
      logger.detachAppender(appender);
      appender.stop();
    }
  }
}
