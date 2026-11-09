package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model;

import lombok.Data;

/** One OR alternative in a compound log filter; populated dimensions are matched with AND. */
@Data
public class CompositeLogFilter {

  private Long userId;
  private Long moduleId;
  private Short componentId;
  private Short eventId;
  private Byte originId;
  private Long sectionId;
  private String ipAddress;
}
