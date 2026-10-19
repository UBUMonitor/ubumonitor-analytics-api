package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseLogsStatisticsResult {

  private List<StatisticRow> rows;
}
