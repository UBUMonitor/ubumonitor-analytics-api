package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.zalando.logbook.HttpHeaders;
import org.zalando.logbook.autoconfigure.LogbookProperties;
import org.zalando.logbook.core.HeaderFilters;
import org.zalando.logbook.core.QueryFilters;
import org.zalando.logbook.json.JsonBodyFilters;

class LogbookConfigTest {

  private static final String SENTINEL = "secret-sentinel";

  @Test
  void masksSensitiveHeadersParametersFormAndJsonValues() throws Exception {
    StandardEnvironment environment = new StandardEnvironment();
    new YamlPropertySourceLoader()
        .load("application-dev", new ClassPathResource("application-dev.yml"))
        .forEach(environment.getPropertySources()::addLast);
    LogbookProperties properties =
        Binder.get(environment)
            .bind("logbook", LogbookProperties.class)
            .orElseThrow(IllegalStateException::new);

    Set<String> sensitiveFields =
        Set.of(
            "password",
            "dbPassword",
            "token",
            "access_token",
            "accessToken",
            "credential",
            "moodleLaunchCredential",
            "wstoken");
    Set<String> sensitiveHeaderNames =
        Set.of(
            "Authorization",
            "X-Moodle-Token",
            "Cookie",
            "password",
            "dbPassword",
            "token",
            "access_token",
            "accessToken",
            "credential",
            "moodleLaunchCredential",
            "wstoken");
    assertTrue(properties.getObfuscate().getHeaders().containsAll(sensitiveHeaderNames));
    assertTrue(properties.getObfuscate().getParameters().containsAll(sensitiveFields));
    assertTrue(properties.getObfuscate().getJsonBodyFields().containsAll(sensitiveFields));
    assertTrue(properties.getObfuscate().getJsonBodyFields().contains("cookies"));
    assertTrue(properties.getObfuscate().getJsonBodyFields().contains("value"));

    HttpHeaders headers =
        HttpHeaders.of(
            "Authorization", SENTINEL, "dbPassword", SENTINEL, "X-Moodle-Token", SENTINEL);
    var filteredHeaders =
        HeaderFilters.replaceHeaders(properties.getObfuscate().getHeaders(), "XXX").filter(headers);
    assertFalse(filteredHeaders.toString().contains(SENTINEL));

    Set<String> parameters = new HashSet<>(properties.getObfuscate().getParameters());
    String filteredQuery =
        QueryFilters.replaceQuery(parameters::contains, "XXX")
            .filter(
                String.join(
                    "&",
                    parameter("password"),
                    parameter("dbPassword"),
                    parameter("accessToken"),
                    parameter("wstoken")));
    assertFalse(filteredQuery.contains(SENTINEL));

    String json =
        "{\"password\":\""
            + SENTINEL
            + "\",\"dbPassword\":\""
            + SENTINEL
            + "\",\"accessToken\":\""
            + SENTINEL
            + "\",\"cookies\":[{\"value\":\""
            + SENTINEL
            + "\"}]}";
    String filteredJson =
        JsonBodyFilters.replaceJsonStringProperty(
                new HashSet<>(properties.getObfuscate().getJsonBodyFields()), "XXX")
            .filter("application/json", json);
    assertFalse(filteredJson.contains(SENTINEL));

    String form =
        String.join(
            "&",
            parameter("password"),
            parameter("dbPassword"),
            parameter("accessToken"),
            parameter("moodleLaunchCredential"),
            parameter("wstoken"));
    String filteredForm =
        new LogbookConfig().bodyFilter().filter("application/x-www-form-urlencoded", form);
    assertFalse(filteredForm.contains(SENTINEL));
  }

  private String parameter(String name) {
    return name + "=" + SENTINEL;
  }
}
