package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.SessionNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.config.JwtProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import javax.crypto.SecretKey;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {


    private final JwtDecoder jwtDecoder;
    private final SecretKey secretKey;
    private final Integer expirationTimeInSeconds;
    public JwtUtils(JwtDecoder jwtDecoder, SecretKey secretKey, JwtProperties jwtProperties) {
        this.jwtDecoder = jwtDecoder;
        this.secretKey = secretKey;
        this.expirationTimeInSeconds = jwtProperties.getExpirationTimeInSeconds();
    }


    public String generateJwtToken(String userName, URI host) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusSeconds(expirationTimeInSeconds != null ? expirationTimeInSeconds : 0);

        // Build Nimbus JWTClaimsSet and sign with HMAC using the shared secretKey
        JWTClaimsSet.Builder nimbusBuilder = new JWTClaimsSet.Builder()
            .jwtID(UUID.randomUUID().toString())
            .subject(userName)
            .issuer("UBUMonitorAnalytics")
            .issueTime(java.util.Date.from(issuedAt))
            .expirationTime(java.util.Date.from(expiration));

        if (host != null) {
            nimbusBuilder.claim("host", host.toString());
        }

        JWTClaimsSet nimbusClaims = nimbusBuilder.build();

        try {
            JWSSigner signer = new MACSigner(secretKey.getEncoded());
            SignedJWT signedJWT = new SignedJWT(new com.nimbusds.jose.JWSHeader(JWSAlgorithm.HS256), nimbusClaims);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new SessionNotFoundException("Error signing JWT", e);
        }
    }

    public Jwt parse(String token) {
        return jwtDecoder.decode(token);
    }

    public boolean isValid(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            log.info("Invalid token", e);
            return false;
        }
    }


}
