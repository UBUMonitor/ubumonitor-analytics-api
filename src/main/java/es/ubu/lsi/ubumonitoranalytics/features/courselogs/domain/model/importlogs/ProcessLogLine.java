package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProcessLogLine {
  private LocalDateTime time;
  private Byte componentId;
  private Short eventId;
  private Integer courseId;
  private Integer userId;
  private Integer moduleId;
  private Byte originId;
  private String ipAddress;
}
