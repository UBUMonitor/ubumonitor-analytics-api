package es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class GradeItem {
  private Integer id;
  private Integer parentId;
  private String name;
  private String itemType;
  private Integer itemInstance;
  private BigDecimal gradeMin;
  private BigDecimal gradeMax;
}
