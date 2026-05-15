package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model;

import lombok.Data;

import java.util.List;


@Data
public class UserEnrolledCourses {
    private Integer userId;
    private List<Enrollment> enrolledCourses;
}

