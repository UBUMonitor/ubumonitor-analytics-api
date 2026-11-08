package es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model;

import java.net.URI;
import lombok.Data;

@Data
public class CourseModule {

  private Long id;

  private String name;

  private String code;

  private URI url;

  private String description;

  private Boolean visible;
  private Long instance;
}
