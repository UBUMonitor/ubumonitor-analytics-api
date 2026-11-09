package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;

/** Parses a chunk of CSV rows, so it can run on its own thread. */
@RequiredArgsConstructor
class LogChunkParse implements Supplier<LogRowParser> {

  private final LogParserService logParserService;
  private final LogContext logContext;
  private final List<CSVRecord> records;

  @Override
  public LogRowParser get() {
    LogRowParser parser = new LogRowParser(logParserService, logContext);
    for (CSVRecord csvRecord : records) {
      parser.parse(csvRecord);
    }
    return parser;
  }
}
