package es.ubu.lsi.moodleanalytics.shared.infrastructure.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret;
    private Integer expirationTimeInSeconds;
}
