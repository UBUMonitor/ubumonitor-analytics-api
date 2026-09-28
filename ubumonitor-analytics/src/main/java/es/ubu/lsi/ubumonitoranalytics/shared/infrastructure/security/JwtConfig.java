package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.config.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

  private static final Random RANDOM = new SecureRandom();

  @Bean
  public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
    byte[] secretBytes =
        Optional.ofNullable(jwtProperties.getSecret())
            .map(s -> s.getBytes(StandardCharsets.UTF_8))
            .orElseGet(
                () -> {
                  byte[] b = new byte[32];
                  RANDOM.nextBytes(b);
                  return b;
                });
    return new SecretKeySpec(secretBytes, "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
    byte[] secretBytes = jwtSecretKey.getEncoded();

    OctetSequenceKey oct =
        new OctetSequenceKey.Builder(secretBytes)
            .algorithm(JWSAlgorithm.HS256)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(UUID.randomUUID().toString())
            .build();

    JWKSet jwkSet = new JWKSet(oct);
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
    return NimbusJwtDecoder.withSecretKey(jwtSecretKey).build();
  }
}
