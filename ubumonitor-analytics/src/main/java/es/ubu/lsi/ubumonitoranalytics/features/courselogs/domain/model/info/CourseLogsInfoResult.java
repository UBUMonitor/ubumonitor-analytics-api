package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import java.util.List;
import lombok.Data;

@Data
public class CourseLogsInfoResult {
  private List<CourseComponent> components;
  private List<CourseEvent> events;
  private List<CourseComponentEvent> componentsEvents;
}
