package es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterInput {
    private String userName;
    private URI host;
    private String hostName;
    private String dbPassword;
}
