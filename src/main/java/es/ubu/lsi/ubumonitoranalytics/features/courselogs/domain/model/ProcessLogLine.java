package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcessLogLine {
    private LocalDateTime time;
    private Byte componentId;
    private Short eventId;
    private Integer courseId;
    private Integer userId;
    private Integer moduleId;
    private Byte originId;
    private String ipAddress;
}
