package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogImportStats {
  private int saved;
  private int ignored;
  private int failed;
}
