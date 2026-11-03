package es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseForums {
  private Long courseId;
  private List<Forum> forums;
}
