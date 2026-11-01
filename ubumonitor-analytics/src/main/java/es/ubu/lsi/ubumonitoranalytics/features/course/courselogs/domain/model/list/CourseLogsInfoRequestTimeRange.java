package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class CourseLogsInfoRequestTimeRange {

  private OffsetDateTime from;
  private OffsetDateTime to;
}
