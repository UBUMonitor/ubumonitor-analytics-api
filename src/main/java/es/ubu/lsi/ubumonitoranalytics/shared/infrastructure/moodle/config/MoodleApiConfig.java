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
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MoodleApiConfig {

    @Bean("moodleRestClient")
    public RestClient moodleRestClient(MoodleConfig moodleConfig,
                                       RestClient.Builder builder,
                                       Logbook logbook) {

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
            .requestInterceptor(new LogbookClientHttpRequestInterceptor(logbook))
            .build();
    }

    @Bean
    public Client moodleClient(RestClientHttpTransport restClientHttpTransport) {
        return new DefaultClient(restClientHttpTransport);
    }
}
