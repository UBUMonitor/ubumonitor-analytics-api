package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config;

import io.krakens.grok.api.Grok;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "moodle")
@Data
public class MoodleRulesConfig {
  private Map<String, Map<String, List<Grok>>> rules;
}
