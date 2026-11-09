package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.moodle;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.csv.MoodleCsvReader;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.exception.MoodleLogExportException;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CsvRecordHandler;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodlePort;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Downloads full or incremental course log exports from Moodle. */
@Component
public class MoodleAdapter implements MoodlePort {

  private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv");

  @Override
  public void downloadAllLogs(Long courseId, RestClient restClient, CsvRecordHandler handler) {
    download(courseId, null, restClient, handler);
  }

  @Override
  public void downloadPartialLogs(
      Long courseId, long since, RestClient restClient, CsvRecordHandler handler) {
    download(courseId, since, restClient, handler);
  }

  /**
   * @param since epoch seconds lower bound sent as Moodle's {@code date} parameter, or {@code null}
   *     to export the whole log
   */
  private void download(
      Long courseId, Long since, RestClient restClient, CsvRecordHandler handler) {
    restClient
        .get()
        .uri(
            uriBuilder -> {
              uriBuilder
                  .path("/report/log/index.php")
                  .queryParam("download", "csv")
                  .queryParam("id", courseId)
                  .queryParam("chooselog", "1")
                  .queryParam("logreader", "logstore_standard");
              if (since != null) {
                uriBuilder.queryParam("date", since);
              }
              return uriBuilder.queryParam("lang", "en").build();
            })
        .exchange(
            (request, response) -> {
              processCsv(handler, response);
              return null;
            });
  }

  private void processCsv(CsvRecordHandler handler, ClientHttpResponse response)
      throws IOException {

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new MoodleLogExportException(
          "Moodle log export returned HTTP status " + response.getStatusCode());
    }

    MediaType contentType = response.getHeaders().getContentType();
    if (contentType == null || !CSV_MEDIA_TYPE.isCompatibleWith(contentType)) {
      throw new MoodleLogExportException(
          "Moodle log export returned unexpected Content-Type: " + contentType);
    }

    try (InputStream body = response.getBody();
        CSVParser parser = MoodleCsvReader.open(body)) {
      for (CSVRecord csvRecord : parser) {
        handler.handle(csvRecord);
      }
    }
  }
}
