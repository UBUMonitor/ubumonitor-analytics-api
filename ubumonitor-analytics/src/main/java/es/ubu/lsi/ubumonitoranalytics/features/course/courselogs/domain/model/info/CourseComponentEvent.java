package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info;

import lombok.Data;

@Data
public class CourseComponentEvent {
  private CourseComponent courseComponent;
  private CourseEvent courseEvent;
}
