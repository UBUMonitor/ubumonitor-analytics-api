package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class Enrollment {
    private User user;
    private Course course;


    private OffsetDateTime lastCourseAccess;

}
