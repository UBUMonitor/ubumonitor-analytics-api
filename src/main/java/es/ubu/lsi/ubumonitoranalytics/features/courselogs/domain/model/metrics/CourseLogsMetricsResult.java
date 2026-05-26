package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseLogsMetricsResult {

    private List<MetricRow> rows;
}
