package es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model;

import java.net.URI;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseSnapshot {
  private String id;
  private String databaseId;
  private String name;
  private Instant createdAt;
  private URI snapshotFile;
}
