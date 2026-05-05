package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionData {
    private String jwt;
    private String userName;

    private URI host;
    private String moodleToken;
    private String dbPassword; // Contraseña encriptada para abrir H2
}
