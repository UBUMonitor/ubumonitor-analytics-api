package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle;

import java.net.URI;
import org.springframework.http.ResponseEntity;

/** Downloads binary resources from Moodle. */
public interface MoodleDownloaderPort {
  /**
   * @param uri Moodle resource URI
   * @param token Moodle web-service token
   * @return the HTTP response containing the downloaded image
   */
  ResponseEntity<byte[]> downloadUserImage(URI uri, String token);
}
