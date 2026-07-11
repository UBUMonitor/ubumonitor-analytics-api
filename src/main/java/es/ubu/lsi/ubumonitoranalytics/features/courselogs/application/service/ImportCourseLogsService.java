package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.MoodlePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportCourseLogsService implements ImportCourseLogsUseCase {

  private static final int BATCH_SIZE = 1000;

  private final LogParserService logParserService;
  private final LogPersistencePort logPersistencePort;
  private final MoodlePort moodlePort;
  private final CurrentSessionContext currentSessionContext;

  // =========================
  // LOCAL FILE IMPORT
  // =========================
  @Override
  @SneakyThrows
  public ProcessLogsResult process(Integer courseId, MultipartFile file) {

    return processStream(
        courseId, file.getInputStream(), logPersistencePort.getLastDateTime(courseId));
  }

  // =========================
  // REMOTE SYNC
  // =========================
  @Override
  @SneakyThrows
  public ProcessLogsResult sync(Integer courseId) {

    SessionData sessionData = currentSessionContext.getSessionData();

    RestClient restClient =
        moodlePort.login(
            sessionData.getUsername(), sessionData.getPassword(), sessionData.getHost());

    LocalDateTime lastLogDateTime = logPersistencePort.getLastDateTime(courseId);

    logPersistencePort.createIfNotExists(courseId);

    Map<String, Byte> logComponents = logPersistencePort.getLogComponents();
    Map<String, Short> logEvents = logPersistencePort.getLogEvents();
    Map<String, Byte> logOrigins = logPersistencePort.getLogOrigins();

    List<ProcessLogLine> batch = new ArrayList<>(BATCH_SIZE);

    AtomicInteger saved = new AtomicInteger(0);
    AtomicInteger ignored = new AtomicInteger(0);
    AtomicInteger failed = new AtomicInteger(0);

    Consumer<CSVRecord> consumer =
        csvRecord ->
            handleRecord(
                courseId,
                csvRecord,
                lastLogDateTime,
                logComponents,
                logEvents,
                logOrigins,
                batch,
                ignored,
                failed);

    if (lastLogDateTime == null || lastLogDateTime.isBefore(LocalDateTime.now().minusDays(30))) {

      moodlePort.downloadAllLogs(courseId, restClient, consumer);

    } else {

      long now = Instant.now().getEpochSecond();
      ZoneOffset zone = ZoneOffset.UTC;

      Instant lastInstant = lastLogDateTime.toInstant(zone);

      Instant safeStart =
          lastInstant.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().minusSeconds(86400);

      long start = safeStart.getEpochSecond();
      long step = 86400;

      for (long ts = start; ts <= now; ts += step) {

        moodlePort.downloadPartialLogs(courseId, ts, restClient, consumer);
      }
    }

    flushBatch(batch, saved, failed);

    return new ProcessLogsResult(saved.get(), ignored.get(), failed.get());
  }

  // =========================
  // CORE PIPELINE (REUSE)
  // =========================
  private ProcessLogsResult processStream(
      Integer courseId, InputStream inputStream, LocalDateTime lastLogDateTime) {

    Map<String, Byte> logComponents = logPersistencePort.getLogComponents();
    Map<String, Short> logEvents = logPersistencePort.getLogEvents();
    Map<String, Byte> logOrigins = logPersistencePort.getLogOrigins();

    logPersistencePort.createIfNotExists(courseId);

    List<ProcessLogLine> batch = new ArrayList<>(BATCH_SIZE);

    AtomicInteger saved = new AtomicInteger(0);
    AtomicInteger ignored = new AtomicInteger(0);
    AtomicInteger failed = new AtomicInteger(0);

    try (BufferedReader reader = buildReader(inputStream);
        CSVParser parser = buildCsvParser(reader)) {

      for (CSVRecord csvRecord : parser) {

        handleRecord(
            courseId,
            csvRecord,
            lastLogDateTime,
            logComponents,
            logEvents,
            logOrigins,
            batch,
            ignored,
            failed);

        if (batch.size() >= BATCH_SIZE) {
          flushBatch(batch, saved, failed);
        }
      }

    } catch (Exception e) {
      log.error("Error processing stream", e);
    }

    flushBatch(batch, saved, failed);

    return new ProcessLogsResult(saved.get(), ignored.get(), failed.get());
  }

  // =========================
  // RECORD HANDLER
  // =========================
  private void handleRecord(
      Integer courseId,
      CSVRecord csvRecord,
      LocalDateTime lastLogDateTime,
      Map<String, Byte> logComponents,
      Map<String, Short> logEvents,
      Map<String, Byte> logOrigins,
      List<ProcessLogLine> batch,
      AtomicInteger ignored,
      AtomicInteger failed) {

    try {
      ProcessLogLine line =
          logParserService.processRow(courseId, csvRecord, logComponents, logEvents, logOrigins);

      if (isValidLog(line, lastLogDateTime)) {
        batch.add(line);
      } else {
        ignored.incrementAndGet();
      }

    } catch (Exception e) {
      log.warn("Error processing CSV row: {}", csvRecord, e);
      failed.incrementAndGet();
    }
  }

  // =========================
  // BATCH FLUSH
  // =========================
  private void flushBatch(List<ProcessLogLine> batch, AtomicInteger saved, AtomicInteger failed) {

    if (batch.isEmpty()) return;

    try {
      logPersistencePort.saveBatch(new ArrayList<>(batch));
      saved.addAndGet(batch.size());
    } catch (Exception e) {
      log.error("Error persisting batch", e);
      failed.addAndGet(batch.size());
    } finally {
      batch.clear();
    }
  }

  // =========================
  // VALIDATION
  // =========================
  private boolean isValidLog(ProcessLogLine line, LocalDateTime lastLogDateTime) {
    return line != null && (lastLogDateTime == null || lastLogDateTime.isBefore(line.getTime()));
  }

  // =========================
  // CSV HELPERS
  // =========================
  @SneakyThrows
  private BufferedReader buildReader(InputStream inputStream) {

    InputStream withBom = BOMInputStream.builder().setInputStream(inputStream).get();

    return new BufferedReader(new InputStreamReader(withBom, StandardCharsets.UTF_8), 64 * 1024);
  }

  private CSVParser buildCsvParser(BufferedReader reader) throws IOException {

    CSVFormat format =
        CSVFormat.DEFAULT
            .builder()
            .setHeader() // 👈 usa la primera fila como header real
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .get();

    return format.parse(reader);
  }
}
