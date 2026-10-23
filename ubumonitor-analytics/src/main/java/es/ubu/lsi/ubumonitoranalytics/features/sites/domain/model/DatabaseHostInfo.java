package es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model;

import java.net.URI;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatabaseHostInfo {
  private URI host;
  private List<DatabaseInfo> databases;
}
