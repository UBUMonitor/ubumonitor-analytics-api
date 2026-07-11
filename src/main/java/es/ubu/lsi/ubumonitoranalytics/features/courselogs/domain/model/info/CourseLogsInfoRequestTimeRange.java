package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseLogsInfoRequestTimeRange {

  private LocalDateTime from;
  private LocalDateTime to;
}
