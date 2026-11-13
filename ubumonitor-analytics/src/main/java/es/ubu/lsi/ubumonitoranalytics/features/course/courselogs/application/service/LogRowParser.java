package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogLine;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;

/**
 * Turns CSV rows into log lines and counts the rows that were skipped or could not be parsed. It
 * touches no database and no shared mutable state, so one instance per thread is safe.
 */
@Slf4j
@RequiredArgsConstructor
class LogRowParser {

  private final LogParserService logParserService;
  private final LogContext logContext;

  @Getter private final List<ProcessLogLine> lines = new ArrayList<>();
  @Getter private int ignored;
  @Getter private int failed;

  /** Parses one row: it ends up in the lines, or counts as ignored or failed. */
  void parse(CSVRecord csvRecord) {
    try {
      ProcessLogLine line =
          logParserService.processRow(
              logContext.getCourseId(),
              logContext.getMoodleTimeZone(),
              csvRecord,
              logContext.getMoodleLogCatalog().getEvents(),
              logContext.getLogOrigins(),
              logContext.getLastLogTime());

      if (line != null) {
        lines.add(line);
      } else {
        ignored++;
      }
    } catch (Exception e) {
      long rowNumber = csvRecord == null ? -1 : csvRecord.getRecordNumber();
      log.warn("Error processing CSV row {} (type={})", rowNumber, e.getClass().getSimpleName());
      failed++;
    }
  }
}
