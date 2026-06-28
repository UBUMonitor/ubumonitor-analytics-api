package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FetchLogLine {

  private Integer id; // ID
  private LocalDateTime timestamp; // TIMESTAMP

  private Integer courseId; // COURSE_ID

  private Integer userId; // USER_ID
  private String userFullName; // USER_FULL_NAME

  private Integer moduleId; // MODULE_ID
  private String moduleName; // MODULE_NAME

  private Integer componentId; // COMPONENT_ID
  private String componentName; // COMPONENT_NAME

  private Integer eventId; // EVENT_ID
  private String eventName; // EVENT_NAME

  private Integer originId; // ORIGIN_ID
  private String originName; // ORIGIN_NAME

  private String ipAddress; // IP_ADDRESS
}
