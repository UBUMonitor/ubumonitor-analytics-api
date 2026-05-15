package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle;
import org.springframework.http.ResponseEntity;

import java.net.URI;

public interface MoodleDownloaderPort {
    ResponseEntity<byte[]> downloadUserImage(URI uri, String token);
}
