package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodlePort;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.client.RestClient;

/** Downloads the logs of one day from Moodle, so it can run on its own thread. */
@RequiredArgsConstructor
class DayLogsDownload implements Supplier<List<CSVRecord>> {

  private final MoodlePort moodlePort;
  private final Long courseId;
  private final long since;
  private final RestClient restClient;

  @Override
  public List<CSVRecord> get() {
    CsvRecordCollector collector = new CsvRecordCollector();
    moodlePort.downloadPartialLogs(courseId, since, restClient, collector);
    return collector.getRecords();
  }
}
