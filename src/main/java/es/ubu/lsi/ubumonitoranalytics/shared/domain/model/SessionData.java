package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionData {
  private String jwt;
  private String username;
  private String password;
  private URI host;
  private String moodleToken;
  private String dbPassword; // Contraseña encriptada para abrir H2
}
