package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CsvRecordHandler;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogImportStats;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;

/**
 * State of one import. Rows are grouped in chunks that are parsed concurrently, while reading and
 * every database write stay on the calling thread (which owns the transaction). Chunks are queued
 * for saving in the order they were received. Not thread-safe: feed it from a single thread.
 */
@Slf4j
@RequiredArgsConstructor
class LogImportRun implements CsvRecordHandler {

  private static final int BATCH_SIZE = 1000;

  /** Rows handed to one parsing task. */
  private static final int PARSE_CHUNK_SIZE = 2000;

  private final AsyncExecutionPort asyncExecution;
  private final LogParserService logParserService;
  private final LogPersistencePort logPersistencePort;
  private final CourseLogsInfoPort courseLogsInfoPort;
  private final Long courseId;
  private final LogContext logContext;

  private List<CSVRecord> chunk = new ArrayList<>(PARSE_CHUNK_SIZE);
  private final Deque<CompletableFuture<LogRowParser>> parsing = new ArrayDeque<>();
  private final List<ProcessLogLine> batch = new ArrayList<>(BATCH_SIZE);
  private int saved;
  private int ignored;
  private int failed;

  /** Queues one CSV row for parsing. */
  @Override
  public void handle(CSVRecord csvRecord) {
    chunk.add(csvRecord);
    if (chunk.size() >= PARSE_CHUNK_SIZE) {
      startParsingChunk();
      absorbFinishedChunks();
    }
  }

  /** Waits for the pending chunks, persists the last batch and builds the result. */
  ProcessLogsResult finish() {
    startParsingChunk();
    while (!parsing.isEmpty()) {
      absorb(parsing.poll().join());
    }
    flush(true);
    return new ProcessLogsResult(
        new LogImportStats(saved, ignored, failed), courseLogsInfoPort.getCourseLogsInfo(courseId));
  }

  private void startParsingChunk() {
    if (chunk.isEmpty()) {
      return;
    }
    parsing.add(asyncExecution.supplyAsync(new LogChunkParse(logParserService, logContext, chunk)));
    chunk = new ArrayList<>(PARSE_CHUNK_SIZE);
  }

  /** Saves the chunks that are already parsed, so writing overlaps with the remaining parsing. */
  private void absorbFinishedChunks() {
    while (!parsing.isEmpty() && parsing.peek().isDone()) {
      absorb(parsing.poll().join());
    }
  }

  private void absorb(LogRowParser parsed) {
    batch.addAll(parsed.getLines());
    ignored += parsed.getIgnored();
    failed += parsed.getFailed();
    flush(false);
  }

  /** Writes full batches, and also the remaining partial one when {@code force} is set. */
  private void flush(boolean force) {
    while (batch.size() >= BATCH_SIZE) {
      save(takeBatch(BATCH_SIZE));
    }
    if (force && !batch.isEmpty()) {
      save(takeBatch(batch.size()));
    }
  }

  private List<ProcessLogLine> takeBatch(int size) {
    List<ProcessLogLine> head = batch.subList(0, size);
    List<ProcessLogLine> taken = new ArrayList<>(head);
    head.clear();
    return taken;
  }

  private void save(List<ProcessLogLine> lines) {
    try {
      logPersistencePort.saveBatch(lines);
      saved += lines.size();
    } catch (Exception e) {
      log.error("Error persisting batch", e);
      failed += lines.size();
    }
  }
}
