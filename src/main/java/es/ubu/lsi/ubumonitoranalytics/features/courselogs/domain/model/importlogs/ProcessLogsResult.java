package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessLogsResult {
  private int saved;
  private int ignored;
  private int failed;
}
