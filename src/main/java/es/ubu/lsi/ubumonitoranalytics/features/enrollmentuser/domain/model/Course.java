package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Course {

    private Integer id;
    private String fullName;
    private String shortName;
    private Boolean showGrades;
    private Boolean enableCompletion;
    private Instant startDate;
    private Instant endDate;
    private Instant timeModified;
}
