package es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model;

import java.util.Collection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseContent {
  private Integer courseId;
  private Collection<Section> sections;
}
