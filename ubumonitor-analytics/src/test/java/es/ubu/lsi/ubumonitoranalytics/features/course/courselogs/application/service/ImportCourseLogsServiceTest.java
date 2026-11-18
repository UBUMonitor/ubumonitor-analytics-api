package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodlePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentMoodleRestClientPort;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ImportCourseLogsServiceTest {

  @Test
  void malformedCsvStopsAtTheFirstMalformedRecordAndDoesNotCountItAsFailed() {
    AsyncExecutionPort asyncExecution = mock(AsyncExecutionPort.class);
    doAnswer(
            invocation -> {
              Supplier<?> task = invocation.getArgument(0);
              return CompletableFuture.completedFuture(task.get());
            })
        .when(asyncExecution)
        .supplyAsync(any());

    LogPersistencePort persistence = mock(LogPersistencePort.class);
    MoodleLogCatalog.Key knownEvent = new MoodleLogCatalog.Key("Activity", "Viewed");
    when(persistence.getLogContext(42L))
        .thenReturn(
            LogContext.builder()
                .courseId(42L)
                .moodleTimeZone(ZoneId.of("UTC"))
                .logOrigins(Map.of("web", (byte) 3))
                .moodleLogCatalog(
                    new MoodleLogCatalog(
                        Map.of(
                            knownEvent,
                            new MoodleLogCatalog.Entry((short) 1, (short) 2, List.of()))))
                .build());

    CourseLogsInfoPort courseLogsInfo = mock(CourseLogsInfoPort.class);
    when(courseLogsInfo.getCourseLogsInfo(42L)).thenReturn(new CourseLogsInfoResult());
    ImportCourseLogsService service =
        new ImportCourseLogsService(
            new LogParserService(),
            persistence,
            courseLogsInfo,
            mock(MoodlePort.class),
            mock(CurrentMoodleRestClientPort.class),
            asyncExecution);
    String csv =
        "Time,Component,Event name,Origin,IP address,Description\n"
            + "\"1/01/24, 12:00:00\",Activity,Viewed,web,127.0.0.1,description\n"
            + "\"unterminated quoted field";

    ProcessLogsResult result =
        service.process(42L, new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

    assertEquals(1, result.getLogImportStats().getSaved());
    assertEquals(0, result.getLogImportStats().getIgnored());
    assertEquals(0, result.getLogImportStats().getFailed());
    verify(persistence).saveBatch(anyList());
    verify(persistence).createIfNotExists(42L);
    verify(courseLogsInfo).getCourseLogsInfo(42L);
  }
}
