package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents one grouped course-log metric row. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricRow {

  private Integer userId;
  private String userFullName;
  private Integer moduleId;
  private String moduleName;
  private Byte componentId;
  private String componentName;
  private Short eventId;
  private String eventName;
  private Byte originId;
  private String originName;
  private String ipAddress;
  private Integer courseId;
  private String timeBucket;
  private String sectionId;
  private String sectionName;
  private Integer value; // Resulting COUNT(*) value.
}
