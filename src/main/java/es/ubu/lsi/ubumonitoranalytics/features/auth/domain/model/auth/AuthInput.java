package es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthInput {
    private String userName;
    private String password;
    private URI host;
    private String dbPassword;
    private String moodleToken;

}

