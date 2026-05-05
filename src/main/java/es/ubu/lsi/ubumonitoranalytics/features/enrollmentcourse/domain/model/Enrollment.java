package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Enrollment {
    private User user;
    private Course course;


    private Instant lastCourseAccess;

}
