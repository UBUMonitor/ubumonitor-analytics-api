package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

import java.net.URI;
import lombok.*;
import org.springframework.web.client.RestClient;

/** Holds the authentication and tenant connection state for an active session. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionData {
  private String token;
  private ActualUser actualUser;
  private Long userId;
  private String username;
  @ToString.Exclude private String password;
  private URI host;

  @ToString.Exclude private String moodleToken;

  @ToString.Exclude private String dbPassword; // Encryption key used to open the tenant database.
  private RestClient restClient;
}
