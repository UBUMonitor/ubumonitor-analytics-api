package es.ubu.lsi.moodleanalytics.shared.infrastructure.moodle.config;



import es.ubu.lsi.moodleadapter.api.generated.api.CoursesApi;
import es.ubu.lsi.moodleadapter.api.generated.api.LoginApi;
import es.ubu.lsi.moodleadapter.api.generated.api.SiteApi;
import es.ubu.lsi.moodleadapter.api.generated.api.UsersApi;
import es.ubu.lsi.moodleadapter.api.generated.invoker.ApiClient;
import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class MoodleApiConfig {

    private final CurrentSessionContext currentSessionContext;

    @Bean("publicRestClient")
    public RestClient publicRestClient(MoodleConfig moodleConfig, RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(moodleConfig.getApi().getConnectTimeout()))
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(moodleConfig.getApi().getReadTimeout());

        return builder
            .requestFactory(requestFactory)
            .baseUrl(moodleConfig.getApi().getBaseUrl())
            .build();
    }

    @Bean("secureRestClient")
    public RestClient secureRestClient(MoodleConfig moodleConfig, RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(moodleConfig.getApi().getConnectTimeout()))
            .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(moodleConfig.getApi().getReadTimeout());

        return builder
            .requestFactory(requestFactory)
            .baseUrl(moodleConfig.getApi().getBaseUrl())
            .requestInterceptor(this::addAuthHeaders)
            .build();

    }

    private ClientHttpResponse addAuthHeaders(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        SessionData sessionData = currentSessionContext.getSessionData();

        if (sessionData != null) {

            request.getHeaders().add("X-Moodle-Token", sessionData.getMoodleToken());
            request.getHeaders().add("X-Moodle-Host", sessionData.getHost().toString());
        }

        return execution.execute(request, body);
    }


    @Bean
    @Primary
    public ApiClient privateApiClient(@Qualifier("secureRestClient") RestClient restClient) {
        return new ApiClient(restClient);
    }

    @Bean("publicApiClient")
    public ApiClient publicApiClient(@Qualifier("publicRestClient") RestClient restClient) {
        return new ApiClient(restClient);
    }

    @Bean
    public LoginApi loginApi(@Qualifier("publicApiClient") ApiClient apiClient) {
        return new LoginApi(apiClient);
    }

    @Bean
    public UsersApi userApi(ApiClient apiClient) {
        return new UsersApi(apiClient);
    }

    @Bean
    public CoursesApi courseApi(ApiClient apiClient) {
        return new CoursesApi(apiClient);
    }

    @Bean
    public SiteApi siteApi(ApiClient apiClient) {
        return new SiteApi(apiClient);
    }
}
