package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.exception.MoodleLogExportException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class MoodleAdapterTest {

  private static final String LOG_EXPORT_URI =
      "http://moodle.test/report/log/index.php"
          + "?download=csv&id=42&chooselog=1&logreader=logstore_standard&lang=en";

  @Test
  void downloadAllLogsParsesCsvWhenStatusAndContentTypeAreValid() {
    RestClientServer client = createMockServer(withSuccess("id,event\n1,viewed", csvType()));
    List<CSVRecord> records = new ArrayList<>();

    new MoodleAdapter().downloadAllLogs(42L, client.restClient(), records::add);

    assertEquals(1, records.size());
    assertEquals("1", records.getFirst().get("id"));
    assertEquals("viewed", records.getFirst().get("event"));
    client.server().verify();
  }

  @Test
  void downloadAllLogsRejectsNonSuccessfulStatus() {
    RestClientServer client =
        createMockServer(withStatus(HttpStatus.BAD_GATEWAY).contentType(csvType()).body("error"));

    MoodleLogExportException exception =
        assertThrows(
            MoodleLogExportException.class,
            () ->
                new MoodleAdapter()
                    .downloadAllLogs(42L, client.restClient(), MoodleAdapterTest::ignoreRecord));

    assertEquals("Moodle log export returned HTTP status 502 BAD_GATEWAY", exception.getMessage());
    client.server().verify();
  }

  @Test
  void downloadAllLogsRejectsNonCsvContentType() {
    RestClientServer client = createMockServer(withSuccess("{}", MediaType.APPLICATION_JSON));

    MoodleLogExportException exception =
        assertThrows(
            MoodleLogExportException.class,
            () ->
                new MoodleAdapter()
                    .downloadAllLogs(42L, client.restClient(), MoodleAdapterTest::ignoreRecord));

    assertEquals(
        "Moodle log export returned unexpected Content-Type: application/json",
        exception.getMessage());
    client.server().verify();
  }

  private RestClientServer createMockServer(
      org.springframework.test.web.client.ResponseCreator responseCreator) {
    RestClient.Builder builder = RestClient.builder().baseUrl("http://moodle.test");
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server
        .expect(requestTo(LOG_EXPORT_URI))
        .andExpect(request -> assertEquals(HttpMethod.GET, request.getMethod()))
        .andRespond(responseCreator);
    return new RestClientServer(server, builder.build());
  }

  private static MediaType csvType() {
    return new MediaType("text", "csv");
  }

  private static void ignoreRecord(CSVRecord csvRecord) {}

  private record RestClientServer(MockRestServiceServer server, RestClient restClient) {}
}
