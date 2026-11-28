package es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model;

import java.net.URI;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseInfo {
  private String id;
  private String userName;
  private URI host;
  private URI databaseFile;

  @Builder.Default private List<DatabaseSnapshot> snapshots = List.of();
}
