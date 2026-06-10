package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config;


import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.core.DefaultClient;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.RestClientHttpTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;


import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MoodleApiConfig {


    @Bean("moodleRestClient")
    public RestClient moodleRestClient(MoodleConfig moodleConfig, RestClient.Builder builder) {

        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(moodleConfig.getApi().getConnectTimeout()))
            .build();

        JdkClientHttpRequestFactory requestFactory =
            new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(moodleConfig.getApi().getReadTimeout());

        ClientHttpRequestFactory bufferingFactory =
            new BufferingClientHttpRequestFactory(requestFactory);

        return builder
            .requestFactory(bufferingFactory)
            .requestInterceptor((request, body, execution) -> {

                log.info("MOODLE REQUEST URI: {}", request.getURI());
                log.info("MOODLE REQUEST BODY: {}",
                    new String(body, StandardCharsets.UTF_8));

                ClientHttpResponse response =
                    execution.execute(request, body);

                String responseBody = new String(
                    response.getBody().readAllBytes(),
                    StandardCharsets.UTF_8);

                log.info("MOODLE RESPONSE STATUS: {}",
                    response.getStatusCode());

                log.info("MOODLE RESPONSE BODY: {}",
                    responseBody);

                return response;
            })
            .build();
    }

    @Bean
    public Client moodleClient(RestClientHttpTransport restClientHttpTransport) {
        return new DefaultClient(restClientHttpTransport);
    }
}
