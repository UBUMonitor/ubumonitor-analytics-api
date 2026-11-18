package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CsvRecordHandler;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.apache.commons.csv.CSVRecord;

/** Keeps every CSV record it receives, in order. Not thread-safe: one instance per download. */
class CsvRecordCollector implements CsvRecordHandler {

  @Getter private final List<CSVRecord> records = new ArrayList<>();

  @Override
  public void handle(CSVRecord csvRecord) {
    records.add(csvRecord);
  }
}
