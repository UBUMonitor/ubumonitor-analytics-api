package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.client.RestClient;

/** Holds the authentication and tenant connection state for an active session. */
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
  private String dbPassword; // Encrypted password used to open the H2 database.
  private RestClient restClient;
}
