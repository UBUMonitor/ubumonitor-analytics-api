package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import java.net.URI;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseInfo {
  private String id;
  private String userName;
  private URI host;
  private URI databaseFile;
}
