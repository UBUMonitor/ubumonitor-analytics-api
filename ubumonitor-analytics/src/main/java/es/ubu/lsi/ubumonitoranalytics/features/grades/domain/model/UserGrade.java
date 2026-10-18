package es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class UserGrade {
  private Integer userId;
  private Integer gradeItemId;
  private BigDecimal gradeRaw;
  private BigDecimal gradePercentageRaw;
}
