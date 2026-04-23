package es.ubu.lsi.moodleanalytics.shared.infrastructure.moodle.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "moodle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoodleConfig {

    private Db db;
    private Api api;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Api {
        private String baseUrl;
        private Integer connectTimeout;
        private Integer readTimeout;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Db {
        private String basePath;
    }
}
