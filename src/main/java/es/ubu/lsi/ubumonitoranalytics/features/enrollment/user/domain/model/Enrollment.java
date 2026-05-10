package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Enrollment {
    private final Course course;
    private final OffsetDateTime lastCourseAccess;
    private Boolean isFavourite;
}
