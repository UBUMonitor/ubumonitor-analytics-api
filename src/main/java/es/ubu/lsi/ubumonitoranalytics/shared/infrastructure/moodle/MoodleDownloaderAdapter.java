package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle;

import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle.MoodleDownloaderPort;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class MoodleDownloaderAdapter implements MoodleDownloaderPort {

    private final RestClient restClient;
    public MoodleDownloaderAdapter(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public byte[] downloadUserImage(URI uri, String token) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(uri).queryParam("token", token);
        return restClient.get()
                .uri(uriComponentsBuilder.toUriString())
                .retrieve()
                .body(byte[].class);
    }
}
