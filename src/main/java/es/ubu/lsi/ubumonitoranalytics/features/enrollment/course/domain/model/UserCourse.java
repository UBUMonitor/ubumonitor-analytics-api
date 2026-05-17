package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model;


import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserCourse {
    private Course course;
    private OffsetDateTime lastCourseAccess;
    private Boolean isFavourite;
}
