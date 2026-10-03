package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import java.util.function.Consumer;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.client.RestClient;

/** Downloads course log CSV records from Moodle. */
public interface MoodlePort {

  /**
   * @param courseId Moodle course identifier
   * @param restClient authenticated Moodle web client
   * @param consumer receives each parsed CSV record
   */
  void downloadAllLogs(Integer courseId, RestClient restClient, Consumer<CSVRecord> consumer);

  /**
   * Downloads records created since the supplied timestamp.
   *
   * @param courseId Moodle course identifier
   * @param since inclusive lower-bound timestamp in epoch seconds
   * @param restClient authenticated Moodle web client
   * @param consumer receives each parsed CSV record
   */
  void downloadPartialLogs(
      Integer courseId, long since, RestClient restClient, Consumer<CSVRecord> consumer);
}
