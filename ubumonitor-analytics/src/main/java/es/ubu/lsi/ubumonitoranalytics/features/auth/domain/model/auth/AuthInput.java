package es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth;

import java.net.URI;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthInput {
  private String username;
  private String password;
  private URI host;
  private String dbPassword;
  private String moodleToken;
  private List<Cookie> cookies;
}
