package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessLogsResult {
  private LogImportStats logImportStats;
  private CourseLogsInfoResult courseLogsInfoResult;
}
