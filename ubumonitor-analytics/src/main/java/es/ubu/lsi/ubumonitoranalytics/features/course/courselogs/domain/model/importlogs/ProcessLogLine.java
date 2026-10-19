package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProcessLogLine {
  private LocalDateTime time;
  private Short componentId;
  private Short eventId;
  private Long courseId;
  private Long userId;
  private Long moduleId;
  private Byte originId;
  private String ipAddress;
}
