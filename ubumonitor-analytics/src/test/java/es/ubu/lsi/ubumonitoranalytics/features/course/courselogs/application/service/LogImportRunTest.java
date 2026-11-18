package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.csv.MoodleCsvReader;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

class LogImportRunTest {

  @Test
  void persistenceFailureContributesToFailedCountAndFinishReturnsRefreshedInfo() throws Exception {
    AsyncExecutionPort asyncExecution = mock(AsyncExecutionPort.class);
    doAnswer(
            invocation -> {
              java.util.function.Supplier<?> task = invocation.getArgument(0);
              return CompletableFuture.completedFuture(task.get());
            })
        .when(asyncExecution)
        .supplyAsync(org.mockito.ArgumentMatchers.any());

    LogPersistencePort persistence = mock(LogPersistencePort.class);
    RuntimeException persistenceFailure = new IllegalStateException("database unavailable");
    org.mockito.Mockito.doThrow(persistenceFailure).when(persistence).saveBatch(anyList());

    CourseLogsInfoPort courseLogsInfo = mock(CourseLogsInfoPort.class);
    CourseLogsInfoResult refreshedInfo = new CourseLogsInfoResult();
    when(courseLogsInfo.getCourseLogsInfo(42L)).thenReturn(refreshedInfo);

    MoodleLogCatalog.Key knownEvent = new MoodleLogCatalog.Key("Activity", "Viewed");
    LogContext context =
        LogContext.builder()
            .courseId(42L)
            .moodleTimeZone(ZoneId.of("UTC"))
            .logOrigins(Map.of("web", (byte) 3))
            .moodleLogCatalog(
                new MoodleLogCatalog(
                    Map.of(
                        knownEvent, new MoodleLogCatalog.Entry((short) 1, (short) 2, List.of()))))
            .build();

    LogImportRun run =
        new LogImportRun(
            asyncExecution, new LogParserService(), persistence, courseLogsInfo, 42L, context);
    String csv =
        "Time,Component,Event name,Origin,IP address,Description\n"
            + "\"1/01/24, 12:00:00\",Activity,Viewed,web,127.0.0.1,description\n"
            + "\"1/01/24, 12:01:00\",Activity,Unknown,web,127.0.0.1,description\n"
            + "invalid-time,Activity,Viewed,web,127.0.0.1,description\n";

    try (CSVParser parser =
        MoodleCsvReader.open(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)))) {
      for (var record : parser) {
        run.handle(record);
      }
    }

    ProcessLogsResult result = run.finish();

    assertEquals(0, result.getLogImportStats().getSaved());
    assertEquals(1, result.getLogImportStats().getIgnored());
    assertEquals(2, result.getLogImportStats().getFailed());
    assertSame(refreshedInfo, result.getCourseLogsInfoResult());
    verify(persistence).saveBatch(anyList());
    verify(courseLogsInfo).getCourseLogsInfo(42L);
  }
}
