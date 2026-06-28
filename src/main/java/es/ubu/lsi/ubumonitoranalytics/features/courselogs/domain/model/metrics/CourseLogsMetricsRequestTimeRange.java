package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseLogsMetricsRequestTimeRange {

  private LocalDateTime from;
  private LocalDateTime to;
}
