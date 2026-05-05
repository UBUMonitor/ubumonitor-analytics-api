package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {


    private final SecretKey secretKey;
    private final Integer expirationTimeInSeconds;
    public JwtUtils(JwtProperties jwtProperties) {
        this.secretKey = Optional.ofNullable(jwtProperties.getSecret())
            .map(String::getBytes)
            .map(Keys::hmacShaKeyFor)
            .orElseGet(()-> Jwts.SIG.HS256.key().build());
        expirationTimeInSeconds = jwtProperties.getExpirationTimeInSeconds();
    }


    public String generateJwtToken(String userName, URI host) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationTimeInSeconds * 1000);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userName)
                .claim("host", host)
                .issuer("UBUMonitorAnalytics")
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            log.info("Invalid token", e);
            return false;
        }
    }


}
