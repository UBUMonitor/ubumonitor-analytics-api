package es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cookie {

  private String name;
  private String value;
  private String domain;
  private String path;
  private Boolean secure;
  private Boolean httpOnly;
}
