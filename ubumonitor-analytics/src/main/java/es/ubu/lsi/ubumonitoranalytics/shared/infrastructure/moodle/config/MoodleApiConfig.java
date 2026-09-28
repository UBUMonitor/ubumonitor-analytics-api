package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.core.DefaultClient;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.RestClientHttpTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MoodleApiConfig {

  @Bean("moodleRestClient")
  public RestClient moodleRestClient(
      MoodleConfig moodleConfig, RestClient.Builder builder, Logbook logbook) {

    ConnectionConfig connectionConfig =
        ConnectionConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(moodleConfig.getApi().getConnectTimeout()))
            .build();

    PoolingHttpClientConnectionManager connectionManager =
        PoolingHttpClientConnectionManagerBuilder.create()
            .setDefaultConnectionConfig(connectionConfig)
            .build();

    RequestConfig requestConfig =
        RequestConfig.custom()
            .setResponseTimeout(Timeout.ofMilliseconds(moodleConfig.getApi().getReadTimeout()))
            .build();

    CloseableHttpClient httpClient =
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);

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
