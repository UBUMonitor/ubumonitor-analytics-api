package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodlePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.config.MoodleRulesConfig;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence.LogPersistenceAdapter;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentMoodleRestClientPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

@SpringJUnitConfig(ImportCourseLogsServiceTransactionTest.TestConfig.class)
class ImportCourseLogsServiceTransactionTest {

  private static final String CSV =
      "Time,Component,Event name,Origin,IP address,Description\n"
          + "\"1/01/24, 12:00:00\",Activity,Viewed,web,127.0.0.1,description\n";

  @Autowired private ImportCourseLogsService service;
  @Autowired private LogPersistencePort persistencePort;
  @Autowired private MoodlePort moodlePort;
  @Autowired private DSLContext dsl;
  @Autowired private LogPersistenceAdapter persistenceAdapter;

  @BeforeEach
  void setUp() {
    LogContext logContext =
        LogContext.builder()
            .courseId(42L)
            .moodleTimeZone(ZoneId.of("UTC"))
            .logOrigins(Map.of("web", (byte) 3))
            .moodleLogCatalog(
                new MoodleLogCatalog(
                    Map.of(
                        new MoodleLogCatalog.Key("Activity", "Viewed"),
                        new MoodleLogCatalog.Entry((short) 1, (short) 2, List.of()))))
            .build();
    when(persistencePort.getLogContext(42L)).thenReturn(logContext);
  }

  @Test
  void csvStreamAndSyncDownloadRunOutsidePersistenceTransactions() {
    doAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              return null;
            })
        .when(persistencePort)
        .createIfNotExists(42L);
    doAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              return null;
            })
        .when(persistencePort)
        .saveBatch(anyList());

    TrackingInputStream csv = new TrackingInputStream(CSV.getBytes(StandardCharsets.UTF_8));
    service.process(42L, csv);
    assertTrue(csv.wasRead());

    doAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              return null;
            })
        .when(moodlePort)
        .downloadAllLogs(any(), any(), any());
    service.sync(42L);
  }

  @Test
  void failedBatchDoesNotLeavePartialLogRows() {
    dsl.execute("DROP TRIGGER IF EXISTS fail_second_log");
    dsl.execute("DROP TABLE IF EXISTS logs");
    dsl.execute(
        """
        CREATE TABLE logs (
          logged_at BIGINT,
          user_id BIGINT,
          course_id BIGINT,
          component_id SMALLINT,
          event_id SMALLINT,
          module_id BIGINT,
          origin_id SMALLINT,
          ip_address TEXT
        )
        """);
    dsl.execute(
        """
        CREATE TRIGGER fail_second_log BEFORE INSERT ON logs
        WHEN NEW.user_id = 2
        BEGIN
          UPDATE logs SET ip_address = 'partial' WHERE user_id = 1;
          SELECT RAISE(ABORT, 'forced persistence failure');
        END
        """);

    assertThrows(
        RuntimeException.class,
        () -> persistenceAdapter.saveBatch(List.of(logLine(1L), logLine(2L))));

    assertEquals(0, dsl.fetchCount(DSL.table("logs")));
  }

  private static ProcessLogLine logLine(Long userId) {
    ProcessLogLine line = new ProcessLogLine();
    line.setTime(java.time.Instant.ofEpochSecond(1));
    line.setUserId(userId);
    line.setCourseId(42L);
    line.setComponentId((short) 1);
    line.setEventId((short) 2);
    line.setOriginId((byte) 3);
    return line;
  }

  private static final class TrackingInputStream extends ByteArrayInputStream {

    private boolean read;

    private TrackingInputStream(byte[] content) {
      super(content);
    }

    @Override
    public synchronized int read() {
      assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
      read = true;
      return super.read();
    }

    @Override
    public synchronized int read(byte[] buffer, int offset, int length) {
      assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
      read = true;
      return super.read(buffer, offset, length);
    }

    boolean wasRead() {
      return read;
    }
  }

  @Configuration
  @EnableTransactionManagement(proxyTargetClass = true)
  static class TestConfig {

    @Bean
    SingleConnectionDataSource dataSource() {
      return new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
    }

    @Bean
    PlatformTransactionManager transactionManager(SingleConnectionDataSource dataSource) {
      return new JdbcTransactionManager(dataSource);
    }

    @Bean
    DSLContext dsl(SingleConnectionDataSource dataSource) {
      return DSL.using(new TransactionAwareDataSourceProxy(dataSource), SQLDialect.SQLITE);
    }

    @Bean
    Jooq jooq(DSLContext dsl) {
      Jooq jooq = mock(Jooq.class);
      when(jooq.dsl()).thenReturn(dsl);
      return jooq;
    }

    @Bean
    LogPersistencePort persistencePort() {
      return mock(LogPersistencePort.class);
    }

    @Bean
    LogPersistenceAdapter logPersistenceAdapter(
        Jooq jooq, MoodleRulesConfig rulesConfig, CurrentSessionPort currentSessionPort) {
      return new LogPersistenceAdapter(jooq, rulesConfig, currentSessionPort);
    }

    @Bean
    MoodleRulesConfig moodleRulesConfig() {
      return mock(MoodleRulesConfig.class);
    }

    @Bean
    CurrentSessionPort currentSessionPort() {
      return mock(CurrentSessionPort.class);
    }

    @Bean
    CourseLogsInfoPort courseLogsInfoPort() {
      CourseLogsInfoPort port = mock(CourseLogsInfoPort.class);
      when(port.getCourseLogsInfo(42L)).thenReturn(new CourseLogsInfoResult());
      return port;
    }

    @Bean
    MoodlePort moodlePort() {
      return mock(MoodlePort.class);
    }

    @Bean
    CurrentMoodleRestClientPort currentMoodleRestClientPort() {
      CurrentMoodleRestClientPort port = mock(CurrentMoodleRestClientPort.class);
      when(port.requireOnlineRestClient()).thenReturn(RestClient.create());
      return port;
    }

    @Bean
    AsyncExecutionPort asyncExecutionPort() {
      AsyncExecutionPort port = mock(AsyncExecutionPort.class);
      doAnswer(
              invocation -> {
                Supplier<?> task = invocation.getArgument(0);
                return CompletableFuture.completedFuture(task.get());
              })
          .when(port)
          .supplyAsync(any());
      return port;
    }

    @Bean
    ImportCourseLogsService service(
        CourseLogsInfoPort courseLogsInfoPort,
        @Qualifier("persistencePort") LogPersistencePort persistencePort,
        MoodlePort moodlePort,
        CurrentMoodleRestClientPort currentMoodleRestClientPort,
        AsyncExecutionPort asyncExecutionPort) {
      return new ImportCourseLogsService(
          new LogParserService(),
          persistencePort,
          courseLogsInfoPort,
          moodlePort,
          currentMoodleRestClientPort,
          asyncExecutionPort);
    }
  }
}
