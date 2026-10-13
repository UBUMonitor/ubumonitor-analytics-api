package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds Moodle API and tenant database settings. */
@ConfigurationProperties(prefix = "moodle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodleConfig {

  private Db db = new Db();
  private Api api = new Api();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Api {
    private Integer connectTimeout;
    private Integer readTimeout;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Db {
    private String basePath;
    private String jdbcUrlTemplate;
  }
}
