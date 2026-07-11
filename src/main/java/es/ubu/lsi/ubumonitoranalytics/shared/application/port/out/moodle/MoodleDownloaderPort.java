package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle;

import java.net.URI;
import org.springframework.http.ResponseEntity;

public interface MoodleDownloaderPort {
  ResponseEntity<byte[]> downloadUserImage(URI uri, String token);
}
