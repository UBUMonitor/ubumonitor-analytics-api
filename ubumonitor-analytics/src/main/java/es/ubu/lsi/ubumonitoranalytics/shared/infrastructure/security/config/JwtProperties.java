package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds JWT signing and expiration settings. */
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
  private String secret;
  private Integer expirationTimeInSeconds;
}
