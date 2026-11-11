package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.csv.MoodleCsvReader;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodlePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

/** Imports course logs from uploaded CSV files or synchronizes them from Moodle. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportCourseLogsService implements ImportCourseLogsUseCase {

  /** Beyond this age of the last stored log, a single full export is cheaper than daily ones. */
  private static final Duration FULL_DOWNLOAD_THRESHOLD = Duration.ofDays(30);

  /** Window requested from Moodle on each incremental download. */
  private static final long INCREMENTAL_STEP_SECONDS = Duration.ofDays(1).toSeconds();

  private final LogParserService logParserService;
  private final LogPersistencePort logPersistencePort;
  private final CourseLogsInfoPort courseLogsInfoPort;
  private final MoodlePort moodlePort;
  private final CurrentSessionPort currentSessionContext;
  private final AsyncExecutionPort asyncOperations;

  /**
   * @param courseId Moodle course identifier
   * @param inputStream uploaded log CSV content
   * @param fileName original uploaded filename
   * @return import statistics and refreshed course log information
   */
  @Override
  @Transactional
  public ProcessLogsResult process(Long courseId, InputStream inputStream, String fileName) {
    LogContext logContext = logPersistencePort.getLogContext(courseId);

    logPersistencePort.createIfNotExists(courseId);
    LogImportRun run = newRun(courseId, logContext);

    try (CSVParser parser = MoodleCsvReader.open(inputStream)) {
      for (CSVRecord csvRecord : parser) {
        run.handle(csvRecord);
      }
    } catch (Exception e) {
      log.error("Error processing stream", e);
    }

    return run.finish();
  }

  /**
   * @param courseId Moodle course identifier
   * @return synchronization statistics and refreshed course log information
   */
  @Override
  @Transactional
  public ProcessLogsResult sync(Long courseId) {
    RestClient restClient = currentSessionContext.getSessionData().getRestClient();

    LogContext logContext = logPersistencePort.getLogContext(courseId);
    logPersistencePort.createIfNotExists(courseId);
    LogImportRun run = newRun(courseId, logContext);

    Instant lastLogTime = logContext.getLastLogTime();
    if (lastLogTime == null || lastLogTime.isBefore(Instant.now().minus(FULL_DOWNLOAD_THRESHOLD))) {
      moodlePort.downloadAllLogs(courseId, restClient, run);
    } else {
      // Downloads run in parallel; rows are still processed and saved here, in day order, because
      // the transaction is bound to this thread.
      for (CompletableFuture<List<CSVRecord>> download :
          startIncrementalDownloads(courseId, lastLogTime, restClient)) {
        for (CSVRecord csvRecord : await(download)) {
          run.handle(csvRecord);
        }
      }
    }

    return run.finish();
  }

  /** Starts one asynchronous download per day, from the last stored log until now. */
  private List<CompletableFuture<List<CSVRecord>>> startIncrementalDownloads(
      Long courseId, Instant lastLogTime, RestClient restClient) {
    long now = Instant.now().getEpochSecond();

    List<CompletableFuture<List<CSVRecord>>> downloads = new ArrayList<>();
    for (long since = lastLogTime.getEpochSecond();
        since <= now;
        since += INCREMENTAL_STEP_SECONDS) {
      downloads.add(
          asyncOperations.supplyAsync(
              new DayLogsDownload(moodlePort, courseId, since, restClient)));
    }
    return downloads;
  }

  /** Waits for a download, rethrowing its original exception instead of a wrapper. */
  private static List<CSVRecord> await(CompletableFuture<List<CSVRecord>> download) {
    try {
      return download.join();
    } catch (CompletionException e) {
      if (e.getCause() instanceof RuntimeException cause) {
        throw cause;
      }
      throw e;
    }
  }

  private LogImportRun newRun(Long courseId, LogContext logContext) {
    return new LogImportRun(
        asyncOperations,
        logParserService,
        logPersistencePort,
        courseLogsInfoPort,
        courseId,
        logContext);
  }
}
