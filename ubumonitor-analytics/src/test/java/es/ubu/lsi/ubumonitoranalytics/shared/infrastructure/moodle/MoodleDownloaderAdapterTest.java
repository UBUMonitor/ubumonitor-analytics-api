package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import es.ubu.lsi.ubumonitoranalytics.shared.application.image.UserPlaceholderImage;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MoodleDownloaderAdapterTest {

  private static final URI IMAGE_URI = URI.create("https://moodle.example.test/user/image.png");

  @Test
  void returnsDownloadedImage() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server
        .expect(requestTo(IMAGE_URI + "?token=sentinel"))
        .andRespond(withSuccess(new byte[] {1, 2, 3}, MediaType.IMAGE_PNG));
    MoodleDownloaderAdapter adapter = new MoodleDownloaderAdapter(builder);

    var response = adapter.downloadUserImage(IMAGE_URI, "sentinel");

    assertEquals(200, response.getStatusCode().value());
    assertArrayEquals(new byte[] {1, 2, 3}, response.getBody());
    server.verify();
  }

  @Test
  void returnsPlaceholderForHttpError() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(IMAGE_URI + "?token=sentinel")).andRespond(withServerError());
    MoodleDownloaderAdapter adapter = new MoodleDownloaderAdapter(builder);

    var response = adapter.downloadUserImage(IMAGE_URI, "sentinel");

    assertFallback(response.getBody());
    server.verify();
  }

  @Test
  void logsHttpFailureWithoutSensitiveUrlOrException() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(IMAGE_URI + "?token=sentinel")).andRespond(withServerError());
    MoodleDownloaderAdapter adapter = new MoodleDownloaderAdapter(builder);
    Logger logger = (Logger) LoggerFactory.getLogger(MoodleDownloaderAdapter.class);
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    try {
      adapter.downloadUserImage(IMAGE_URI, "sentinel");
      String logMessages =
          appender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .collect(java.util.stream.Collectors.joining("\n"));
      assertTrue(logMessages.contains("httpStatus=500"));
      assertFalse(logMessages.contains("sentinel"));
      assertFalse(logMessages.contains("moodle.example.test"));
      server.verify();
    } finally {
      logger.detachAppender(appender);
      appender.stop();
    }
  }

  @Test
  void returnsPlaceholderForTimeout() {
    RestClient.Builder builder =
        RestClient.builder()
            .requestFactory((_, _) -> {
              throw new ResourceAccessException("Read timed out");
            });
    MoodleDownloaderAdapter adapter = new MoodleDownloaderAdapter(builder);

    var response = adapter.downloadUserImage(IMAGE_URI, "sentinel");

    assertFallback(response.getBody());
  }

  @Test
  void returnsPlaceholderForEmptyResponseBody() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server
        .expect(requestTo(IMAGE_URI + "?token=sentinel"))
        .andRespond(withSuccess(new byte[0], MediaType.IMAGE_PNG));
    MoodleDownloaderAdapter adapter = new MoodleDownloaderAdapter(builder);

    var response = adapter.downloadUserImage(IMAGE_URI, "sentinel");

    assertEquals(200, response.getStatusCode().value());
    // An empty successful response is returned as-is; the placeholder is only used on exceptions.
    org.junit.jupiter.api.Assertions.assertNull(response.getBody());
    server.verify();
  }

  private static void assertFallback(byte[] body) {
    assertNotNull(body);
    assertArrayEquals(UserPlaceholderImage.getPlaceholder(), body);
  }
}
