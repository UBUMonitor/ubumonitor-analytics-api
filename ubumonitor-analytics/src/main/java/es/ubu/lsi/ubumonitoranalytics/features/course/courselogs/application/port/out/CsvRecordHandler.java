package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out;

import org.apache.commons.csv.CSVRecord;

/** Receives the CSV records of a Moodle log export, one at a time and in order. */
public interface CsvRecordHandler {

  /**
   * @param csvRecord one parsed row of the export
   */
  void handle(CSVRecord csvRecord);
}
