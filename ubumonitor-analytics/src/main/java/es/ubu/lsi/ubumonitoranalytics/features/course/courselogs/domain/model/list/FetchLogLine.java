package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class FetchLogLine {

  private Long id; // ID
  private OffsetDateTime timestamp; // LOGGED_AT

  private Long courseId; // COURSE_ID

  private Long userId; // USER_ID
  private String userFullName; // USER_FULL_NAME

  private Long moduleId; // MODULE_ID
  private String moduleName; // MODULE_NAME
  private Long sectionId; // SECTION_ID
  private String sectionName; // SECTION_NAME

  private Integer componentId; // COMPONENT_ID
  private String componentName; // COMPONENT_NAME

  private Integer eventId; // EVENT_ID
  private String eventName; // EVENT_NAME

  private Integer originId; // ORIGIN_ID
  private String originName; // ORIGIN_NAME

  private String ipAddress; // IP_ADDRESS
}
