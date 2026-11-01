package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class CourseLogsStatisticsRequestTimeRange {

  private OffsetDateTime from;
  private OffsetDateTime to;
}
