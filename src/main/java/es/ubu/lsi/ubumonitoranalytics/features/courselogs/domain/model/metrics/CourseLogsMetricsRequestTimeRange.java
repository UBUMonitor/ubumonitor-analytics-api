package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseLogsMetricsRequestTimeRange {

    private LocalDateTime from;
    private LocalDateTime to;
}
