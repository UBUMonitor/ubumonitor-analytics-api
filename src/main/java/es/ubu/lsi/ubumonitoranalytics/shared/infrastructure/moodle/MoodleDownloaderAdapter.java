package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle.MoodleDownloaderPort;
import es.ubu.lsi.ubumonitoranalytics.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@Slf4j
public class MoodleDownloaderAdapter implements MoodleDownloaderPort {

    private final RestClient restClient;
    public MoodleDownloaderAdapter(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public ResponseEntity<byte[]> downloadUserImage(URI uri, String token) {
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(uri).queryParam("token", token);
            return restClient.get()
                .uri(uriComponentsBuilder.toUriString())
                .retrieve()
                .toEntity(byte[].class);
        } catch (Exception e) {
            log.error("Error downloading user image from Moodle: {}", uri, e);
            return ResponseEntity.status(200).contentType(MediaType.IMAGE_PNG).body(ImageUtil.getPlaceholder());
        }

    }
}
