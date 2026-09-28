package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import java.util.function.Consumer;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.client.RestClient;

public interface MoodlePort {

  void downloadAllLogs(Integer courseId, RestClient restClient, Consumer<CSVRecord> consumer);

  void downloadPartialLogs(
      Integer courseId, long since, RestClient restClient, Consumer<CSVRecord> consumer);
}
