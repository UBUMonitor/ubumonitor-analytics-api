package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Enrollment {
    private User user;
    private Course course;


    private OffsetDateTime lastCourseAccess;

}

