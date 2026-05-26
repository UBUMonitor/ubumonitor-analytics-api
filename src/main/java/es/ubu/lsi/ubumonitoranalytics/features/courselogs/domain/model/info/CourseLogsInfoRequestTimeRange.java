package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseLogsInfoRequestTimeRange {

    private LocalDateTime from;
    private LocalDateTime to;
}
