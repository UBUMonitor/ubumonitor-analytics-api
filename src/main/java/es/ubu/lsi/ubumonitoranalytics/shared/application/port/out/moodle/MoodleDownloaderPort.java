package es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle;
import java.net.URI;

public interface MoodleDownloaderPort {
    byte[] downloadUserImage(URI uri, String token);
}
