package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Course {

    private Integer id;
    private String fullName;
    private String shortName;
    private Boolean showGrades;
    private Boolean enableCompletion;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private OffsetDateTime timeModified;
}

