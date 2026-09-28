package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseLogsMetricsResult {

  private List<MetricRow> rows;
}
