package es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class GradeItem {
  private Long id;
  private Long parentId;
  private Long categoryId;
  private Long parentCategoryId;
  private String name;
  private String itemType;
  private Long itemInstance;
  private Long moduleId;
  private Integer position;
  private BigDecimal gradeMin;
  private BigDecimal gradeMax;
}
