package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.statistics;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseLogsStatisticsRequestTimeRange {

  private LocalDateTime from;
  private LocalDateTime to;
}
