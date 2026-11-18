package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.config;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.core.BodyFilters;

@Configuration
public class LogbookConfig {

  private static final String REPLACEMENT = "XXX";

  @Bean
  @Primary
  public BodyFilter bodyFilter() {
    BodyFilter formFilter =
        BodyFilters.replaceFormUrlEncodedProperty(
            Set.of(
                "wstoken",
                "password",
                "token",
                "access_token",
                "dbPassword",
                "accessToken",
                "credential",
                "moodleLaunchCredential"),
            REPLACEMENT);
    return BodyFilter.merge(BodyFilters.defaultValue(), formFilter);
  }
}
