package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CourseLogsStatisticsRequestTimeRange {

  private LocalDateTime from;
  private LocalDateTime to;
}
