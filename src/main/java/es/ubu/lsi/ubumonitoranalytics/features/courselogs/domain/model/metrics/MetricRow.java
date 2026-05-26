package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricRow {

    private Integer userId;
    private String userFullName; // NUEVO
    private Integer moduleId;
    private String moduleName; // NUEVO
    private Byte componentId;
    private String componentName; // NUEVO
    private Short eventId;
    private String eventName; // NUEVO
    private Byte originId;
    private String originName; // NUEVO
    private String ipAddress;
    private Integer courseId;
    private String timeBucket;
    private Integer value; // El COUNT(*) resultante
}
