package es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterInput {
    private String username;
    private URI host;
    private String hostName;
    private String dbPassword;
}

