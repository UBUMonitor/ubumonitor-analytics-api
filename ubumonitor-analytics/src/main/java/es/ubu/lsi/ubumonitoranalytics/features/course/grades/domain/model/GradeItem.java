package es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class GradeItem {
  private Long id;
  private Long parentId;
  private String name;
  private String itemType;
  private Long itemInstance;
  private BigDecimal gradeMin;
  private BigDecimal gradeMax;
}
