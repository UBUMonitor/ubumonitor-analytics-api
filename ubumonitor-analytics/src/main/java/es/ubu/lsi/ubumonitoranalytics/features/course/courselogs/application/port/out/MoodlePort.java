package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out;

import org.springframework.web.client.RestClient;

/** Downloads course log CSV records from Moodle. */
public interface MoodlePort {

  /**
   * @param courseId Moodle course identifier
   * @param restClient authenticated Moodle web client
   * @param handler receives each parsed CSV record
   */
  void downloadAllLogs(Long courseId, RestClient restClient, CsvRecordHandler handler);

  /**
   * Downloads records created since the supplied timestamp.
   *
   * @param courseId Moodle course identifier
   * @param since inclusive lower-bound timestamp in epoch seconds
   * @param restClient authenticated Moodle web client
   * @param handler receives each parsed CSV record
   */
  void downloadPartialLogs(
      Long courseId, long since, RestClient restClient, CsvRecordHandler handler);
}
