package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.SessionNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtils {


    private final JwtDecoder jwtDecoder;
    private final SecretKey secretKey;



    public String generateJwtToken(String username, URI host) {
        Instant issuedAt = Instant.now();

        // Build Nimbus JWTClaimsSet and sign with HMAC using the shared secretKey
        JWTClaimsSet.Builder nimbusBuilder = new JWTClaimsSet.Builder()
            .jwtID(UUID.randomUUID().toString())
            .subject(username)
            .issuer("UBUMonitorAnalytics")
            .issueTime(java.util.Date.from(issuedAt));

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
