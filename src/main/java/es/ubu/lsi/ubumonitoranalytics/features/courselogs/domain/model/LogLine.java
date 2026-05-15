package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogLine {
    private LocalDateTime time;
    private byte componentId;
    private short eventId;
    private int courseId;
    private Integer userId;
    private Integer moduleId;
}
