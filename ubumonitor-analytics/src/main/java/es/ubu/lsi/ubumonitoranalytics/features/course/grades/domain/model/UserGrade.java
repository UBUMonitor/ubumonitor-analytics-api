package es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class UserGrade {
  private Long userId;
  private Long gradeItemId;
  private BigDecimal gradeRaw;
  private BigDecimal gradePercentageRaw;
}
