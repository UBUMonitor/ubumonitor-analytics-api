package es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model;

import java.util.List;
import lombok.Data;

@Data
public class Section {

  private Integer id;
  private Integer position;
  private String name;
  private String summary;
  private Boolean visible;
  private List<CourseModule> modules;
}
