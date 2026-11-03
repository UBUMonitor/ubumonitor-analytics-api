package es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarEvent {
  private Long id;
  private String name;
  private String description;
  private Long courseId;
  private Long categoryId;
  private Long groupId;
  private Long userId;
  private Long repeatId;
  private String moduleName;
  private Long instance;
  private String eventType;
  private Long timeStart;
  private Long timeDuration;
  private Integer visible;
  private Integer sequence;
  private Long timeModified;
  private Long subscriptionId;
  private String uuid;
}
